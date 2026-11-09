package ru.practicum.user.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.common.UserMapper;
import ru.practicum.user.common.UserRepository;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.event.RegistrationRequestProcessedEvent;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserRole;
import ru.practicum.user.request.RegistrationRequestService;
import ru.practicum.user.request.model.RegistrationRequest;
import ru.practicum.user.request.model.RegistrationStatus;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserServiceImpl implements AdminUserService, UserDetailsService {
    UserRepository userRepository;
    RegistrationRequestService registrationRequestService;
    PasswordEncoder passwordEncoder;
    ApplicationEventPublisher eventPublisher;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание записи с данными нового пользователя в БД");
        String encodePassword = passwordEncoder.encode(newUserRequest.getPassword());
        User user = UserMapper.mapToUser(newUserRequest, encodePassword);
        UserDto userDto = UserMapper.mapToDto(userRepository.save(user));
        log.info("Сгенерирован ID {} для нового пользователя", userDto.getId());
        return userDto;
    }

    @Override
    public UserDto createUserFromRequest(long requestId, UserRole role) {
        log.info("Создание записи с данными нового пользователя в БД по запросу с ID {}", requestId);
        RegistrationRequest request = registrationRequestService.findById(requestId);

        if (request.getStatus() != RegistrationStatus.PENDING) {
            throw new ConditionsNotMetException("Only requests with a status of PENDING can be approved.");
        }

        User user = UserMapper.mapToUser(request, role);
        UserDto userDto = UserMapper.mapToDto(userRepository.save(user));
        log.info("Сгенерирован ID {} для нового пользователя", userDto.getId());

        registrationRequestService.changeRequestStatusById(requestId, RegistrationStatus.APPROVED);

        log.info("Публикация события об одобрении заявки для email: {}", request.getEmail());
        eventPublisher.publishEvent(new RegistrationRequestProcessedEvent(
                request.getEmail(),
                request.getName(),
                RegistrationStatus.APPROVED
        ));

        return userDto;
    }

    @Override
    public void rejectRequest(long requestId) {
        RegistrationRequest request = registrationRequestService.findById(requestId);
        if (request.getStatus() != RegistrationStatus.PENDING) {
            throw new ConditionsNotMetException("Only requests with a status of PENDING can be rejected.");
        }
        registrationRequestService.changeRequestStatusById(requestId, RegistrationStatus.REJECTED);

        log.info("Публикация события об отклонении заявки для email: {}", request.getEmail());
        eventPublisher.publishEvent(new RegistrationRequestProcessedEvent(
                request.getEmail(),
                request.getName(),
                RegistrationStatus.REJECTED
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            List<Long> filteredIds = ids.stream()
                    .filter(Objects::nonNull)
                    .toList();

            log.info("Получение списка пользователей по конкретным ID: {}", filteredIds);
            users = userRepository.findAllById(filteredIds);
        } else {
            log.info("Получение списка всех пользователей с пагинацией: from = {}, size = {}", from, size);
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
            users = userRepository.findAll(pageable).getContent();
        }

        List<UserDto> userDtos = users.stream()
                .map(UserMapper::mapToDto)
                .toList();
        log.info("Получен список из {} пользователей", userDtos.size());
        return userDtos;
    }

    @Override
    public User getUserById(long id) {
        log.info("Запрос на получение информации о пользователе с ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка получения: пользователь с ID {} не найден", id);
                    return new NotFoundException(String.format("User with ID %s was not found", id));
                });
        log.info("Получена информация о пользователе с ID {}", id);
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        log.info("Запрос на получение информации о пользователе с email {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException(("User with email = " + email
                        + " was not found")));
        log.info("Получена информация о пользователе с email {}", email);
        return user;
    }

    @Override
    public User getCurrentAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserByEmail(email);
    }

    @Override
    public void updateRole(long userId, UserRole newRole) {
        log.info("Обновление роли пользователя с ID {}", userId);

        User user = getUserById(userId);

        if (!user.isSimpleUser()) {
            log.warn("Не удалось обновить роль: пользователь с ID {} не является обычным пользователем (SimpleUser)", userId);
            throw new ConditionsNotMetException("Only regular users can be promoted to admin.");
        }

        user.setRole(newRole);
        log.info("Роль пользователя с ID {} успешно обновлена на {}", userId, newRole);
    }

    @Override
    public void deleteUserById(long id) {
        log.info("Удаление данных пользователя с ID {} из БД", id);

        User currentAdmin = getCurrentAdmin();
        User userToBeDeleted = getUserById(id);

        if (userToBeDeleted.isSuperAdmin()) {
            throw new ConditionsNotMetException("System error: Deleting a Super Admin is prohibited at the " +
                    "application level.");
        }

        if (!currentAdmin.isSuperAdmin() && userToBeDeleted.isAdmin()) {
            throw new ConditionsNotMetException("Access denied: A standard administrator cannot delete " +
                    "other administrators.");
        }

        try {
            RegistrationRequest registrationRequest = registrationRequestService.findByEmail(
                    userToBeDeleted.getEmail(),
                    RegistrationStatus.APPROVED
            );
            registrationRequestService.changeRequestStatusById(registrationRequest.getId(),
                    RegistrationStatus.REJECTED);
            log.info("Заявка пользователя [{}] успешно переведена в статус REJECTED", userToBeDeleted.getEmail());
        } catch (NotFoundException e) {
            log.warn("Удаляемый пользователь [{}] не имел активной APPROVED-заявки в системе",
                    userToBeDeleted.getEmail());
        }

        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        log.info("Загрузка пользователя для Spring Security по email: {}", email);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email = " + email + " was not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().toAuthority())
                .build();
    }
}

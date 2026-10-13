package ru.practicum.user;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание записи с данными нового пользователя в БД");
        User user = UserMapper.mapToUser(newUserRequest);
        UserDto userDto = UserMapper.mapToDto(userRepository.save(user));
        log.info("Сгенерирован ID {} для нового пользователя", userDto.getId());
        return userDto;
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
    public void deleteUserById(long id) {
        checkUserExists(id);

        log.info("Удаление данных пользователя с ID {} из БД", id);
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} успешно удален", id);
    }


    @Override
    public void checkUserExists(long id) {
        log.info("Проверка существования пользователя с ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("Ошибка проверки: пользователь с ID {} не зарегистрирован в системе", id);
            throw new NotFoundException(String.format("User with ID %s not found", id));
        }
    }
}

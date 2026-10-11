package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание записи с данными нового пользователя в БД");
        User user = UserMapper.mapToUser(newUserRequest);
        UserDto userDto = UserMapper.mapToDto(userRepository.save(user));
        log.debug("Сгенерирован ID {} для нового пользователя", userDto.getId());
        return userDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {

        List<Long> filteredIds = (ids == null) ? List.of() : ids.stream()
                .filter(Objects::nonNull)
                .toList();

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));

        if (!filteredIds.isEmpty()) {
            log.info("Получение списка пользователей по списку ID размером {}", filteredIds.size());
            List<UserDto> userDtos = userRepository.findAllByIdIn(filteredIds, pageable).getContent()
                    .stream()
                    .map(UserMapper::mapToDto)
                    .toList();
            log.debug("Получен список из {} пользователей", userDtos.size());
            return userDtos;
        }

        log.info("Получение списка всех пользователей from = {}, size = {}", from, size);

        List<UserDto> userDtos = userRepository.findAll(pageable).getContent()
                .stream()
                .map(UserMapper::mapToDto)
                .toList();
        log.debug("Получен список из {} пользователей", userDtos.size());
        return userDtos;
    }

    @Override
    public User getUserById(long id) {
        log.info("Запрос на получение информации о пользователе с ID {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with ID %s was not found", id)));
        log.debug("Получена информация о пользователе с ID {}", id);
        return user;
    }

    @Override
    public void deleteUserById(long id) {
        log.info("Удаление данных пользователя с ID {} из БД", id);
        userRepository.deleteById(id);
        log.debug("Запрос на удаление пользователя с ID {} обработан", id);
    }


    @Override
    public void checkUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User with ID %s not found", userId));
        }
    }
}

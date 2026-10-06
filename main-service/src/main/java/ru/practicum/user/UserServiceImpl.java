package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto create(NewUserRequest dto) {
        log.info("Создание записи с данными нового пользователя в БД");
        User user = UserMapper.mapToUser(dto);
        UserDto userDto = UserMapper.mapToDto(userRepository.save(user));
        log.debug("Пользователь успешно сохранен с ID {}", userDto.getId());
        return userDto;
    }

    @Override
    public void delete(long id) {
        log.info("Удаление данных пользователя с ID {} из БД", id);
        userRepository.deleteById(id);
        log.debug("Пользователь с ID {} успешно удалён из БД", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        if (ids != null && !ids.isEmpty()) {
            log.info("Получение списка пользователей по списку ID - {}", ids);
            List<User> users = userRepository.findAllById(ids);
            if (users.isEmpty()) {
                log.debug("Пользователи не найдены. Возвращается пустой список");
                return List.of();
            }
            List<UserDto> userDtos = users.stream()
                    .map(UserMapper::mapToDto)
                    .toList();
            log.debug("Отправляется список из {} пользователей", userDtos.size());
            return userDtos;
        }

        log.info("Получение списка всех пользователей from = {}, size = {}", from, size);
        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(from, size, sort);
        Page<User> userPage = userRepository.findAll(pageable);
        List<UserDto> userDtos = userPage.getContent().stream()
                .map(UserMapper::mapToDto)
                .toList();
        log.debug("Отправляется список из {} пользователей", userDtos.size());
        return userDtos;
    }
}
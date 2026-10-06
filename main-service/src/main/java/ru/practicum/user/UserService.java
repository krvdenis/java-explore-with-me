package ru.practicum.user;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(NewUserRequest dto);

    void delete(long id);

    List<UserDto> getUsers(List<Long> ids, int from, int size);
}
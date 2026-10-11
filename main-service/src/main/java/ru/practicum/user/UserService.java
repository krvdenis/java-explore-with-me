package ru.practicum.user;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUserById(long id);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    User getUserById(long id);

    void checkUserExists(long userId);
}
package ru.practicum.user;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    User getUserById(long id);

    void deleteUserById(long id);

    void checkUserExists(long id);
}
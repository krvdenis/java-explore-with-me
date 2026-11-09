package ru.practicum.user.admin;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserRole;

import java.util.List;

public interface AdminUserService {
    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    User getUserById(long id);

    void deleteUserById(long id);

    void rejectRequest(long requestId);

    UserDto createUserFromRequest(long requestId, UserRole role);

    User getUserByEmail(String email);

    User getCurrentAdmin();

    void updateRole(long userId, UserRole newRole);
}
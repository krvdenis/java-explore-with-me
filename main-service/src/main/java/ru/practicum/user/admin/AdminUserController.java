package ru.practicum.user.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.UserRole;

import java.util.List;

import static ru.practicum.util.Constants.PATH_ADMIN;
import static ru.practicum.util.Constants.PATH_REJECT;
import static ru.practicum.util.Constants.PATH_REQUESTS;
import static ru.practicum.util.Constants.PATH_REQUEST_ID;
import static ru.practicum.util.Constants.PATH_USERS;
import static ru.practicum.util.Constants.PATH_USER_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_ADMIN + PATH_USERS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {
    AdminUserService adminUserService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(
            @Valid @RequestBody NewUserRequest newUserRequest,
            @RequestParam(defaultValue = "USER") UserRole role) {
        log.info("Запрос на создание пользователя {}", newUserRequest);
        return adminUserService.createUser(newUserRequest);
    }

    @PostMapping(PATH_REQUESTS + PATH_REQUEST_ID)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUserFromRequest(
            @PathVariable long requestId,
            @RequestParam(defaultValue = "USER") UserRole role) {
        log.info("Запрос на создание пользователя по запросу с ID {}", requestId);
        return adminUserService.createUserFromRequest(requestId, role);
    }

    @PatchMapping(PATH_REQUESTS + PATH_REQUEST_ID + PATH_REJECT)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectRequest(@PathVariable long requestId) {
        log.info("Запрос на отклонение запроса с ID {} на регистрацию пользователя", requestId);
        adminUserService.rejectRequest(requestId);
    }

    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение списка пользователей по их ID, from = {}, size = {}", from, size);
        return adminUserService.getUsers(ids, from, size);
    }

    @DeleteMapping(PATH_USER_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable long userId) {
        log.info("Запрос на удаление пользователя с ID {}", userId);
        adminUserService.deleteUserById(userId);
    }

    //реализовать выдачу админки
}

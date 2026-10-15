package ru.practicum.user;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

import static ru.practicum.util.Constants.PATH_ADMIN;
import static ru.practicum.util.Constants.PATH_USERS;
import static ru.practicum.util.Constants.PATH_USER_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_ADMIN + PATH_USERS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserController {
    UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("Запрос на создание пользователя {}", newUserRequest);
        return userService.createUser(newUserRequest);
    }

    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение списка пользователей по их ID, from = {}, size = {}", from, size);
        return userService.getUsers(ids, from, size);
    }

    @DeleteMapping(PATH_USER_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable long userId) {
        log.info("Запрос на удаление пользователя с ID = {}", userId);
        userService.deleteUserById(userId);
    }
}

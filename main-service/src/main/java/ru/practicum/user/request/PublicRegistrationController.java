package ru.practicum.user.request;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.user.request.dto.NewRegistrationRequest;
import ru.practicum.user.request.dto.RegistrationRequestDto;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/registration")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicRegistrationController {
    RegistrationRequestService registrationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RegistrationRequestDto createRegistrationRequest(@Valid @RequestBody NewRegistrationRequest newRequest) {
        log.info("Запрос на создание запроса на регистрацию пользователя {}", newRequest);
        return registrationRequestService.createRequest(newRequest);
    }

}

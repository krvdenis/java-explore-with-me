package ru.practicum.request.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.priv.dto.ParticipationRequestDto;

import java.util.List;

import static ru.practicum.util.Constants.PATH_CANCEL;
import static ru.practicum.util.Constants.PATH_REQUESTS;
import static ru.practicum.util.Constants.PATH_REQUEST_ID;
import static ru.practicum.util.Constants.PATH_USERS;
import static ru.practicum.util.Constants.PATH_USER_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_USERS + PATH_USER_ID + PATH_REQUESTS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateRequestController {
    RequestService requestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable long userId,
            @RequestParam long eventId) {
        log.info("Создание запроса от пользователя с ID {} на участие в событии с ID {}", userId, eventId);
        return requestService.create(userId, eventId);
    }

    @GetMapping
    public List<ParticipationRequestDto> getAllRequests(@PathVariable long userId) {
        log.info("Запрос на получение информации о заявках пользователя с ID {} на участие в чужих событиях", userId);
        return requestService.getAll(userId);
    }

    @PatchMapping(PATH_REQUEST_ID + PATH_CANCEL)
    public ParticipationRequestDto cancelRequestById(@PathVariable long userId, @PathVariable long requestId) {
        log.info("Запрос на отмену своей заявки с ID {} на участие в чужом событии", requestId);
        return requestService.cancelRequestById(userId, requestId);
    }

}

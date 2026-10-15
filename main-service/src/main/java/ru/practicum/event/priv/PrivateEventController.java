package ru.practicum.event.priv;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.priv.dto.NewEventDto;
import ru.practicum.event.priv.dto.UpdateEventUserRequest;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.priv.dto.ParticipationRequestDto;

import java.util.List;

import static ru.practicum.util.Constants.PATH_EVENTS;
import static ru.practicum.util.Constants.PATH_EVENT_ID;
import static ru.practicum.util.Constants.PATH_REQUESTS;
import static ru.practicum.util.Constants.PATH_USERS;
import static ru.practicum.util.Constants.PATH_USER_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_USERS + PATH_USER_ID + PATH_EVENTS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateEventController {
    PrivateEventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable long userId, @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Запрос на создание события {}", newEventDto);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping(PATH_EVENT_ID)
    public EventFullDto getOwnedEventById(@PathVariable long userId, @PathVariable long eventId) {
        log.info("Запрос на получение информации о событии с ID {}, созданном пользователем с ID {}", userId, eventId);
        return eventService.getOwnedEventDtoById(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getOwnedEvents(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение информации о своих событиях from = {}, size = {}, созданных пользователем с ID {}",
                from, size, userId);
        return eventService.getOwnedEvents(userId, from, size);
    }

    @PatchMapping(PATH_EVENT_ID)
    public EventFullDto updateEvent(
            @PathVariable long userId, @PathVariable long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Запрос на обновление информации о своём событии с ID {}, {}", eventId, updateEventUserRequest);
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @GetMapping(PATH_EVENT_ID + PATH_REQUESTS)
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable long userId, @PathVariable long eventId) {
        log.info(("Запрос на получении информации о запросах на участие в своём событии с ID {}, созданном " +
                "пользователем с ID {}"), eventId, userId);
        return eventService.getEventRequestsForInitiator(userId, eventId);
    }

    @PatchMapping(PATH_EVENT_ID + PATH_REQUESTS)
    public EventRequestStatusUpdateResult changeRequestsStatus(
            @PathVariable long userId,
            @PathVariable long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("Запрос на изменения статуса заявок на событие с ID {} пользователя с ID {} на {}",
                eventId, userId, statusUpdateRequest);
        return eventService.changeRequestsStatusForInitiator(userId, eventId, statusUpdateRequest);
    }
}

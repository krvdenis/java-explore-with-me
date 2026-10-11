package ru.practicum.event.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    public static final String PATH_EVENT_ID = "/{eventId}";
    public static final String PATH_REQUESTS = "/requests";
    private final PrivateEventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable long userId, @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Запрос создание события {}", newEventDto);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping(PATH_EVENT_ID)
    public EventFullDto getOwnedEventById(@PathVariable long userId,
                                          @PathVariable long eventId) {
        log.info("Запрос на получение информации о событии с ID {}, созданном пользователем с ID {}", userId,
                eventId);
        return eventService.getOwnedEventDtoById(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getOwnedEvents(
            @PathVariable long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение информации о событиях from = {}, size = {}, созданных пользователем с ID {}",
                from, size, userId);
        return eventService.getOwnedEvents(userId, from, size);
    }

    @PatchMapping(PATH_EVENT_ID)
    public EventFullDto updateEvent(@PathVariable long userId, @PathVariable long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Запрос на обновление информации о событии с ID {}, {}", eventId, updateEventUserRequest);
        return eventService.updateEvent(userId, eventId, updateEventUserRequest);
    }


    @GetMapping(PATH_EVENT_ID + PATH_REQUESTS)
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable long userId, @PathVariable long eventId) {
        log.info(("Запрос на получении информации о запросах на участие в событии с ID {}, созданное " +
                "пользователем с ID {}"), eventId, userId);
        return eventService.getEventRequestsForInitiator(userId, eventId);
    }

    @PatchMapping(PATH_EVENT_ID + PATH_REQUESTS)
    public EventRequestStatusUpdateResult changeRequestsStatus(
            @PathVariable long userId,
            @PathVariable long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("Запрос на изменения статуса заявок на событие с ID {} пользователя с ID {} на {}",
                eventId, userId, statusUpdateRequest.getStatus());
        return eventService.changeRequestsStatusForInitiator(userId, eventId, statusUpdateRequest);
    }
}

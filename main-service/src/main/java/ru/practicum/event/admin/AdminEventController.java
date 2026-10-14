package ru.practicum.event.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.EventState;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.util.Constants.PATH_ADMIN;
import static ru.practicum.util.Constants.PATH_EVENTS;
import static ru.practicum.util.Constants.PATH_EVENT_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_ADMIN + PATH_EVENTS)
public class AdminEventController {
    private final AdminEventService adminEventService;

    @PatchMapping(PATH_EVENT_ID)
    public EventFullDto updateEvent(
            @PathVariable long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Запрос администратора на редактирование данных события с ID {} и его статуса {}", eventId,
                updateEventAdminRequest);
        return adminEventService.updateEvent(eventId, updateEventAdminRequest);
    }

    @GetMapping
    public List<EventFullDto> getEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос администратора на получение событий с фильтрацией");
        return adminEventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}

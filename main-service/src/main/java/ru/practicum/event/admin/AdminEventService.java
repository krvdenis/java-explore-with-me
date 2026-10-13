package ru.practicum.event.admin;

import ru.practicum.event.common.State;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AdminEventService {

    EventFullDto updateEvent(long id, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventFullDto> getEvents(
            List<Long> users,
            List<State> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size
    );

    boolean checkEventExistsByCategoryId(long categoryId);

    Set<Event> getEvents(Set<Long> eventIds);
}

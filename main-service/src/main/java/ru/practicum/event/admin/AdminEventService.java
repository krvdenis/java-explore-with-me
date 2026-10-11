package ru.practicum.event.admin;

import ru.practicum.event.admin.dto.UpdateEventAdminRequest;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.enums.State;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminEventService {

    EventFullDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    boolean checkEventExistsByCategoryId(long categoryId);

    List<EventFullDto> getEvents(
            List<Long> users,
            List<State> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size);
}

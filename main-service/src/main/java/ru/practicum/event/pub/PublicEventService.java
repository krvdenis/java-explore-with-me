package ru.practicum.event.pub;

import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.common.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {
    EventFullDto getPublishedEventDtoById(long id);

    Event getEventById(long id);

    List<EventShortDto> getPublishedEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            boolean onlyAvailable,
            EventSort sort,
            int from,
            int size
    );

}

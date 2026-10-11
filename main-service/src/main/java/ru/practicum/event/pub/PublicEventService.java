package ru.practicum.event.pub;

import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.pub.enums.EventSort;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {
    EventFullDto getPublishedEventDtoById(long eventId);

    Event getPublishedEventById(long eventId);

    Event getEventEntityById(long eventId);

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

//    void checkEventExists(long eventId);


}

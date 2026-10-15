package ru.practicum.event.priv;

import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.priv.dto.NewEventDto;
import ru.practicum.event.priv.dto.UpdateEventUserRequest;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.priv.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {
    EventFullDto createEvent(long userId, NewEventDto newEventDto);

    EventFullDto getOwnedEventDtoById(long userId, long eventId);

    List<EventShortDto> getOwnedEvents(long userId, int from, int size);

    List<ParticipationRequestDto> getEventRequestsForInitiator(long userId, long eventId);

    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent);

    EventRequestStatusUpdateResult changeRequestsStatusForInitiator(
            long userId,
            long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest
    );
}

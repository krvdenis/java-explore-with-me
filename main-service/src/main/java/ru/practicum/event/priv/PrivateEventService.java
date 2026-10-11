package ru.practicum.event.priv;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.priv.dto.NewEventDto;
import ru.practicum.event.priv.dto.UpdateEventUserRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface PrivateEventService {
    @Transactional
    @SuppressWarnings("ConstantConditions")
    EventFullDto createEvent(long userId, NewEventDto newEventDto);

    EventFullDto getOwnedEventDtoById(long userId, long eventId);

    List<EventShortDto> getOwnedEvents(long userId, int from, int size);

    @Transactional
    EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEvent);

    List<ParticipationRequestDto> getEventRequestsForInitiator(long userId, long eventId);

    EventRequestStatusUpdateResult changeRequestsStatusForInitiator(
            long userId,
            long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest
    );
}

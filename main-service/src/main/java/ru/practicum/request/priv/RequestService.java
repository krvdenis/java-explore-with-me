package ru.practicum.request.priv;

import ru.practicum.request.priv.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.priv.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto create(long userId, long eventId);

    List<ParticipationRequestDto> getAll(long userId);

    ParticipationRequestDto cancelRequestById(long userId, long requestId);

    List<ParticipationRequestDto> getEventRequestsByInitiator(long eventId);

    EventRequestStatusUpdateResult changeRequestsStatusByInitiatorEvent(
            EventRequestStatusUpdateRequest statusUpdateRequest,
            long availableSlots
    );
}

package ru.practicum.request;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto create(long userId, long eventId);

    List<ParticipationRequestDto> getAll(long userId);

    ParticipationRequestDto cancelRequestById(long userId, long requestId);

    List<ParticipationRequestDto> getEventRequestsByInitiator(long eventId);

    EventRequestStatusUpdateResult changeRequestsStatusByOwnerEvent(
            EventRequestStatusUpdateRequest statusUpdateRequest,
            long availableSlots
    );
}

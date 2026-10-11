package ru.practicum.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.pub.PublicEventService;
import ru.practicum.event.common.enums.State;
import ru.practicum.event.common.model.Event;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.request.mapper.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.user.UserService;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestServiceImpl implements RequestService {
    RequestRepository requestRepository;
    PublicEventService eventService;
    UserService userService;

    @Override
    public ParticipationRequestDto create(long userId, long eventId) {
        User user = userService.getUserById(userId);
        Event event = eventService.getEventEntityById(eventId);

        validateParticipationRequest(userId, eventId, event);

        boolean isAutoConfirm = event.getParticipantLimit() == 0 || !Boolean.TRUE.equals(event.getRequestModeration());

        RequestStatus initialStatus = isAutoConfirm ? RequestStatus.CONFIRMED : RequestStatus.PENDING;

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(initialStatus)
                .build();

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
        }

        ParticipationRequestDto requestDto = ParticipationRequestMapper.mapToDto(requestRepository.save(request));
        return requestDto;
    }

    @Override
    public List<ParticipationRequestDto> getAll(long userId) {
        userService.checkUserExists(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(ParticipationRequestMapper::mapToDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto cancelRequestById(long userId, long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with ID %s was not found", requestId)));

        if (!Objects.equals(userId, request.getRequester().getId())) {
            throw new ConditionsNotMetException("The initiator of an event cannot add a request to participate in " +
                    "their own event.");
        }

        Event event = request.getEvent();
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            request.getEvent().decrementConfirmedRequests();
        }
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequestDto requestDto = ParticipationRequestMapper.mapToDto(request);
        return requestDto;
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByInitiator(long eventId) {
        List<ParticipationRequestDto> requestDtos = requestRepository.findAllByEventIdOrderByCreatedDesc(eventId)
                .stream()
                .map(ParticipationRequestMapper::mapToDto)
                .toList();

        return requestDtos;
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestsStatusByInitiator(
            EventRequestStatusUpdateRequest statusUpdateRequest,
            long availableSlots
    ) {
        List<Long> requestIds = statusUpdateRequest.getRequestIds();
        RequestStatus requiredStatus = statusUpdateRequest.getStatus();
        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);

        boolean hasNonPendingRequests = requests.stream()
                .allMatch(request -> request.getStatus() != RequestStatus.PENDING);

        if (hasNonPendingRequests) {
            throw new ConditionsNotMetException("Only pending requests can be processed.");
        }

        if (requiredStatus == RequestStatus.CANCELED) {
            requests.forEach(request -> request.setStatus(RequestStatus.CANCELED));
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(requests.stream()
                            .map(ParticipationRequestMapper::mapToDto)
                            .toList())
                    .build();
        } else if (requiredStatus == RequestStatus.CONFIRMED) {

        }

        List<ParticipationRequest> requestsToChange = new ArrayList<>();
        List<ParticipationRequest> requestsToReject = new ArrayList<>();

        for (int i = 0; i < requestIds.size(); i++) {
            if (i < availableSlots) {
                requestsToChange.add(requests.get(i));
            } else {
                requestsToReject.add(requests.get(i));
            }
        }

        requestsToChange.forEach(request -> request.setStatus(requiredStatus));
        requestsToReject.forEach(request -> request.setStatus(RequestStatus.CANCELED));

        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestsToReject.stream()
                        .map(ParticipationRequestMapper::mapToDto)
                        .toList())
                .rejectedRequests(requestsToReject.stream()
                        .map(ParticipationRequestMapper::mapToDto)
                        .toList())
                .build();

        return eventRequestStatusUpdateResult;
    }

    private void validateParticipationRequest(long userId, long eventId, Event event) {
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new DataIntegrityViolationException("could not execute statement; SQL [n/a]; constraint " +
                    "[uq_request]; nested exception is org.hibernate.exception.ConstraintViolationException: could" +
                    " not execute statement");
        }

        if (Objects.equals(userId, event.getInitiator().getId())) {
            throw new ConditionsNotMetException("The initiator of an event cannot add a request to participate in " +
                    "their own event.");
        }

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConditionsNotMetException("You cannot participate in an unpublished event.");
        }

        if (event.getRequestModeration().equals(Boolean.FALSE)) {
            if (event.isParticipantLimitReached()) {
                throw new ConditionsNotMetException("The participant limit for this event has been reached.");
            }
        }
    }
}

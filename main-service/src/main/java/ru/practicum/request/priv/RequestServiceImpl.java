package ru.practicum.request.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.common.EventState;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.pub.PublicEventService;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.priv.dto.ParticipationRequestDto;
import ru.practicum.user.User;
import ru.practicum.user.UserService;

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
        log.info("Получен запрос на создание заявки на участие от пользователя ID: {} для события ID: {}", userId,
                eventId);

        User user = userService.getUserById(userId);
        Event event = eventService.getEventById(eventId);
        log.info("Количество свободных мест для заявок - {}",
                event.getParticipantLimit() - event.getConfirmedRequests());

        validateParticipationRequest(userId, eventId, event);

        boolean isAutoConfirm = false;

        if (event.getParticipantLimit() == 0) {
            isAutoConfirm = true;
        } else if (event.getRequestModeration().equals(Boolean.FALSE)) {
            isAutoConfirm = true;
        }

        RequestStatus initialStatus = isAutoConfirm ? RequestStatus.CONFIRMED : RequestStatus.PENDING;

        log.info("Определен начальный статус заявки: {}, автоподтверждение: {}", initialStatus, isAutoConfirm);

        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(user)
                .status(initialStatus)
                .build();

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            event.incrementConfirmedRequests();
        }
        log.info("Количество подтвержденных заявок для события ID: {} увеличено до: {}", eventId,
                event.getConfirmedRequests());

        ParticipationRequestDto requestDto = ParticipationRequestMapper.mapToDto(requestRepository.save(request));
        log.info("Заявка успешно создана и сохранена с ID: {}", requestDto.getId());
        return requestDto;
    }

    @Override
    public List<ParticipationRequestDto> getAll(long userId) {
        log.info("Получен запрос на получение всех заявок пользователя с ID: {}", userId);

        userService.checkUserExists(userId);

        List<ParticipationRequest> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        log.info("Найдено {} заявок пользователя с ID: {}", requests.size(), userId);

        return requests.stream()
                .map(ParticipationRequestMapper::mapToDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto cancelRequestById(long userId, long requestId) {
        log.info("Получен запрос от пользователя с ID: {} на отмену заявки с ID: {}", userId, requestId);

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Ошибка получения: запрос с ID {} не найден", requestId);
                    return new NotFoundException(String.format("Request with ID %s was not found", requestId));
                });

        if (!Objects.equals(userId, request.getRequester().getId())) {
            log.warn("Пользователь ID: {} не является автором заявки ID: {}", userId, requestId);
            throw new NotFoundException("Participation request with id " + requestId + " was not found for user " +
                    "with id " + userId);
        }

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            throw new ConditionsNotMetException(String.format("Request with ID %d cannot be changed because it " +
                    "already has status CONFIRMED.", request.getId())
            );
        }

        request.setStatus(RequestStatus.CANCELED);
        log.info("Статус заявки ID: {} успешно изменен на CANCELED", requestId);

        return ParticipationRequestMapper.mapToDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByInitiator(long eventId) {
        log.info("Получен запрос от инициатора события на получение заявок для события с ID: {}", eventId);

        List<ParticipationRequestDto> requestDtos = requestRepository.findAllByEventIdOrderByCreatedDesc(eventId)
                .stream()
                .map(ParticipationRequestMapper::mapToDto)
                .toList();

        log.info("Найдено {} заявок для события ID: {}", requestDtos.size(), eventId);
        return requestDtos;
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestsStatusByInitiatorEvent(
            EventRequestStatusUpdateRequest statusUpdateRequest,
            long availableSlots) {
        log.info("Получен запрос на изменение статуса заявок инициатором. Запрашиваемый статус: {}, " +
                        "доступно слотов: {}, список ID: {}", statusUpdateRequest.getStatus(), availableSlots,
                statusUpdateRequest.getRequestIds());

        List<Long> requestIds = statusUpdateRequest.getRequestIds();
        RequestStatus requiredStatus = statusUpdateRequest.getStatus();
        List<ParticipationRequest> requests = requestRepository.findAllById(requestIds);

        boolean hasNonPendingRequests = requests.stream()
                .anyMatch(request -> request.getStatus() != RequestStatus.PENDING);

        if (hasNonPendingRequests) {
            log.warn("Попытка обработать заявки, которые не находятся в статусе PENDING");
            throw new ConditionsNotMetException("Only pending requests can be processed.");
        }

        if (requiredStatus == RequestStatus.REJECTED) {
            requests.forEach(request -> request.setStatus(RequestStatus.REJECTED));
            log.info("Все {} заявок были отменены инициатором", requests.size());

            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(List.of())
                    .rejectedRequests(requests.stream()
                            .map(ParticipationRequestMapper::mapToDto)
                            .toList())
                    .build();
        }

        log.info("Начало процесса подтверждения заявок");
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
        requestsToReject.forEach(request -> request.setStatus(RequestStatus.REJECTED));

        log.info("Обработка завершена. Подтверждено заявок: {}, отклонено: {}", requestsToChange.size(),
                requestsToReject.size());

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(requestsToChange.stream()
                        .map(ParticipationRequestMapper::mapToDto)
                        .toList())
                .rejectedRequests(requestsToReject.stream()
                        .map(ParticipationRequestMapper::mapToDto)
                        .toList())
                .build();
    }

    private void validateParticipationRequest(long userId, long eventId, Event event) {
        log.info("Валидация заявки: пользователь ID: {}, событие ID: {}", userId, eventId);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            log.warn("Валидация провалена: пользователь ID: {} уже подал заявку на событие ID: {}", userId, eventId);
            throw new ConditionsNotMetException("You have already submitted a request to participate in this event.");
        }

        if (Objects.equals(userId, event.getInitiator().getId())) {
            log.warn("Валидация провалена: пользователь ID: {} является инициатором события ID: {}", userId, eventId);
            throw new ConditionsNotMetException("The initiator of an event cannot add a request to participate in " +
                    "their own event.");
        }

        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Валидация провалена: событие ID: {} не опубликовано (текущий статус: {})", eventId,
                    event.getState());
            throw new ConditionsNotMetException("You cannot participate in an unpublished event.");
        }

        if (event.isParticipantLimitReached()) {
            log.warn("Валидация провалена: лимит участников для события ID: {} исчерпан", eventId);
            throw new ConditionsNotMetException("The participant limit for this event has been reached.");
        }

        log.info("Валидация для пользователя ID: {} и события ID: {} успешно пройдена", userId, eventId);
    }
}

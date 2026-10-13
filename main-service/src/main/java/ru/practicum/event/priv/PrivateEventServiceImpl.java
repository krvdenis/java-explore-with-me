package ru.practicum.event.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.common.Category;
import ru.practicum.category.pub.PublicCategoryService;
import ru.practicum.event.common.EventMapper;
import ru.practicum.event.common.EventRepository;
import ru.practicum.event.common.State;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShort;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.util.EventStatsHelper;
import ru.practicum.event.priv.dto.NewEventDto;
import ru.practicum.event.priv.dto.UpdateEventUserRequest;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.priv.RequestService;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.priv.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.priv.dto.ParticipationRequestDto;
import ru.practicum.user.User;
import ru.practicum.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.util.Constants.START_OF_TIME;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateEventServiceImpl implements PrivateEventService {
    private static final int MIN_HOURS_BEFORE_EVENT = 2;

    EventRepository eventRepository;
    UserService userService;
    PublicCategoryService categoryService;
    RequestService requestService;
    EventStatsHelper eventStatsHelper;

    @Override
    @Transactional
    public EventFullDto createEvent(long userId, NewEventDto newEventDto) {
        log.info("Получен запрос на создание события от пользователя с ID: {}", userId);

        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());

        validateEventDate(newEventDto.getEventDate());

        Event event = EventMapper.mapToEvent(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);

        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        } else {
            event.setRequestModeration(newEventDto.getRequestModeration());
        }

        EventFullDto eventFullDto = EventMapper.mapToFullDto(eventRepository.save(event));
        eventFullDto.setViews(0L);

        log.info("Сгенерирован ID {} для нового события", eventFullDto.getId());
        return eventFullDto;
    }

    @Override
    public EventFullDto getOwnedEventDtoById(long userId, long eventId) {
        log.info("Получен запрос от пользователя с ID: {} на просмотр полной информации о своем событии ID: {}",
                userId, eventId);

        Event event = getOwnedEventById(userId, eventId);
        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(eventStatsHelper.getViewsCountForOneEvent(event));

        log.info("Получена полная информация о событии с ID: {} для пользователя ID: {}", eventId, userId);
        return eventFullDto;
    }


    @Override
    public List<EventShortDto> getOwnedEvents(long userId, int from, int size) {
        log.info("Получен запрос на просмотр списка событий пользователя c ID: {} (от: {}, размер: {})", userId, from,
                size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("eventDate"));

        Map<Long, EventShort> eventMap = eventRepository.findAllByInitiatorId(userId, pageable).getContent().stream()
                .collect(Collectors.toMap(EventShort::getId, Function.identity()));

        if (eventMap.isEmpty()) {
            log.info("Список найденных событий пуст");
            return List.of();
        }

        List<String> uris = eventMap.keySet().stream()
                .map(eventId -> String.format(EventStatsHelper.EVENT_URI_TEMPLATE, eventId))
                .toList();

        log.info("Запрос статистики просмотров для {} URI", uris.size());
        Map<Long, ViewStatsDto> eventIdToViewStats = eventStatsHelper.getViewStatsDtos(
                        START_OF_TIME,
                        LocalDateTime.now(),
                        uris,
                        true
                ).stream()
                .collect(Collectors.toMap(viewStatsDto -> {
                            String[] uriParts = viewStatsDto.getUri().split("/");
                            return Long.valueOf(uriParts[uriParts.length - 1]);
                        },
                        Function.identity()));

        List<EventShortDto> eventShortDtos = eventMap.values().stream()
                .map(event -> {
                    EventShortDto eventShortDto = EventMapper.mapToShortDto(event);
                    ViewStatsDto viewStats = eventIdToViewStats.get(event.getId());
                    Long views = viewStats == null ? 0L : viewStats.getHits();
                    eventShortDto.setViews(views);
                    return eventShortDto;
                })
                .toList();

        log.info("Сформирован краткий список из {} событий пользователя ID: {}", eventShortDtos.size(), userId);
        return eventShortDtos;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
        log.info("Получен запрос на обновление события c ID: {} пользователем ID: {}", eventId, userId);

        Event event = getOwnedEventById(userId, eventId);

        validateEventState(event);

        if (updateEventUserRequest.getEventDate() != null) {
            validateEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.hasCategory()) {
            Category category = categoryService.getCategoryById(updateEventUserRequest.getCategory());
            event.setCategory(category);
            log.info("В событии ID: {} обновлена категория", eventId);
        }

        EventMapper.updateEventFields(event, updateEventUserRequest);

        if (updateEventUserRequest.hasStateAction()) {
            log.info("Обработка изменения статуса события c ID: {} действием: {}", eventId,
                    updateEventUserRequest.getStateAction());
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }

        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(eventStatsHelper.getViewsCountForOneEvent(event));

        log.info("Событие ID: {} пользователя ID: {} успешно обновлено", eventId, userId);
        return eventFullDto;
    }


    @Override
    public List<ParticipationRequestDto> getEventRequestsForInitiator(long userId, long eventId) {
        log.info("Получен запрос от инициатора события с ID: {} на выгрузку заявок к событию ID: {}", userId, eventId);

        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            log.warn("Событие ID: {} с инициатором ID: {} не существует", eventId, userId);
            throw new NotFoundException(String.format("Event with ID %s was not found", eventId));
        }

        List<ParticipationRequestDto> requestDtos = requestService.getEventRequestsByInitiator(eventId);
        log.info("Для события ID: {} найдено {} заявок", eventId, requestDtos.size());
        return requestDtos;
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult changeRequestsStatusForInitiator(
            long userId,
            long eventId,
            EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("Получен запрос от инициатора события с ID: {} на изменение статуса заявок к событию ID: {} на {}",
                userId, eventId, statusUpdateRequest.getStatus());

        Event event = getOwnedEventById(userId, eventId);

        if (event.isParticipantLimitReached()) {
            log.warn("Изменение статуса заявок прервано: для события ID: {} достигнут лимит участников", eventId);
            throw new ConditionsNotMetException("The participant limit for this event has been reached.");
        }

        long availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
        log.info("Событие ID: {}, доступно свободных слотов для подтверждения: {}", eventId, availableSlots);

        EventRequestStatusUpdateResult result = requestService.changeRequestsStatusByInitiatorEvent(
                statusUpdateRequest,
                availableSlots
        );

        List<ParticipationRequestDto> confirmedRequests = result.getConfirmedRequests();
        int countConfirmedRequest = 0;

        while (countConfirmedRequest < confirmedRequests.size()) {
            event.incrementConfirmedRequests();
            countConfirmedRequest++;
        }

        log.info("Количество подтвержденных заявок {}", event.getConfirmedRequests());
        log.info("Обновлены статусы заявок для события ID: {}. Число подтвержденных счетчиков увеличено на {}",
                eventId, countConfirmedRequest);
        return result;
    }

    private Event getOwnedEventById(long initiator, long eventId) {
        log.info("Поиск события по ID: {} и инициатору с ID: {}", eventId, initiator);
        return eventRepository.findByIdAndInitiatorId(eventId, initiator)
                .orElseThrow(() -> {
                    log.warn("Событие ID: {} у инициатора ID: {} не найдено", eventId, initiator);
                    return new NotFoundException(String.format("Event with ID %s was not found", eventId));
                });
    }

    private static void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(MIN_HOURS_BEFORE_EVENT))) {
            log.warn("Валидация даты провалена: дата события {} должна быть минимум на {} часа позже текущего момента",
                    eventDate, MIN_HOURS_BEFORE_EVENT);

            throw new ConditionsNotMetException(String.format("Field: eventDate. Error: must be at least %d hours in" +
                    " the future from the current moment. Value: %s", MIN_HOURS_BEFORE_EVENT, eventDate));
        }
    }

    private static void validateEventState(Event event) {
        if (event.getState() == State.PUBLISHED) {
            log.warn("Валидация изменения статуса провалена: событие ID: {} уже имеет статус PUBLISHED", event.getId());
            throw new ConditionsNotMetException("Only pending or canceled events can be changed");
        }
    }

}

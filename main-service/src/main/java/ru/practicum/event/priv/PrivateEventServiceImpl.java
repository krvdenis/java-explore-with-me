package ru.practicum.event.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.pub.PublicCategoryService;
import ru.practicum.category.common.model.Category;
import ru.practicum.event.common.dto.EventShort;
import ru.practicum.event.common.enums.State;
import ru.practicum.event.common.repository.EventRepository;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.priv.dto.NewEventDto;
import ru.practicum.event.priv.dto.UpdateEventUserRequest;
import ru.practicum.event.common.mapper.EventMapper;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.util.EventStatsHelper;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.RequestService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.user.UserService;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateEventServiceImpl implements PrivateEventService {
    EventRepository eventRepository;
    UserService userService;
    PublicCategoryService categoryService;
    RequestService requestService;
    EventStatsHelper eventStatsHelper;

    @Override
    @Transactional
    @SuppressWarnings("ConstantConditions")
    public EventFullDto createEvent(long userId, NewEventDto newEventDto) {
        log.info("Получен запрос на создание события от пользователя ID: {}", userId);
        User user = userService.getUserById(userId);
        Category category = categoryService.getCategoryById(newEventDto.getCategory());

        validateEventDate(newEventDto.getEventDate());

        log.info("Инфо о юзере {}",user);
        Event event = EventMapper.mapToEvent(newEventDto, user, category);
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        } else {
            event.setRequestModeration(newEventDto.getRequestModeration());
        }
        log.info("Инфо о инициаторе события {}",event.getInitiator().getId());
        EventFullDto eventFullDto = EventMapper.mapToFullDto(eventRepository.save(event));
        eventFullDto.setViews(0L);

        log.debug("Сгенерирован ID {} для нового события", eventFullDto.getId());
        return eventFullDto;
    }

    @Override
    public EventFullDto getOwnedEventDtoById(long userId, long eventId) {
//        userService.checkUserExists(userId);
        log.info("Получен запрос пользователя ID: {} на просмотр полной информации о своем событии ID: {}", userId, eventId);

        Event event = getOwnedEventById(userId, eventId);
        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(eventStatsHelper.getViewsCountForOneEvent(eventId));

        log.info("Успешно получена полная информация о событии ID: {} для пользователя ID: {}", eventId, userId);
        return eventFullDto;
    }


    @Override
    public List<EventShortDto> getOwnedEvents(long userId, int from, int size) {
//        userService.checkUserExists(userId);
        log.info("Получен запрос на просмотр списка событий пользователя ID: {} (от: {}, размер: {})", userId, from, size);

        int page = from / size;
        Sort sort = Sort.by("eventDate");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EventShort> eventPage = eventRepository.findAllByInitiatorId(userId, pageable);

        log.debug("Из репозитория получено страниц: {}, всего элементов: {}", eventPage.getTotalPages(), eventPage.getTotalElements());

        Map<Long, EventShort> eventShortMap = eventPage.getContent().stream()
                .collect(Collectors.toMap(EventShort::getId, Function.identity()));

        List<String> uris = eventShortMap.keySet().stream()
                .map(eventId -> String.format(EventStatsHelper.EVENT_URI_TEMPLATE, eventId))
                .toList();
        List<ViewStatsDto> viewStatsDtos = eventStatsHelper.getViewStatsDtos(
                LocalDateTime.now().minusYears(5),
                LocalDateTime.now().plusYears(5),
                uris,
                true
        );

        Map<Long, ViewStatsDto> eventIdToViewStats = viewStatsDtos.stream()
                .collect(Collectors.toMap(
                        viewStatsDto -> {
                            String[] uri = viewStatsDto.getUri().split("/");
                            Long eventId = Long.valueOf(uri[uri.length - 1]);
                            return eventId;
                        },
                        Function.identity()));

        List<EventShortDto> eventShortDtos = eventShortMap.values().stream()
                .map(event -> {
                    EventShortDto eventShortDto = EventMapper.mapToShortDto(event);
                    ViewStatsDto viewStatsDto = eventIdToViewStats.get(event.getId());
                    Long views = viewStatsDto == null ? 0L : viewStatsDto.getHits();
                    eventShortDto.setViews(views);
                    return eventShortDto;
                })
                .toList();

        log.info("Успешно сформирован краткий список из {} событий пользователя ID: {}", eventShortDtos.size(), userId);
        return eventShortDtos;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest updateEventUserRequest) {
//        userService.checkUserExists(userId);
        log.info("Получен запрос на обновление события ID: {} пользователем ID: {}", eventId, userId);

        Event event = getOwnedEventById(userId, eventId);

        validateEventState(event);

        if (updateEventUserRequest.getEventDate() != null) {
            validateEventDate(updateEventUserRequest.getEventDate());
        }

        if (updateEventUserRequest.hasCategory()) {
            Category category = categoryService.getCategoryById(updateEventUserRequest.getCategory());
            event.setCategory(category);
            log.debug("В событии ID: {} обновлена категория", eventId);
        }

        EventMapper.updateEventFields(event, updateEventUserRequest);

        if (updateEventUserRequest.hasStateAction()) {
            log.debug("Обработка изменения статуса события ID: {} на действие: {}", eventId, updateEventUserRequest.getStateAction());
            switch (updateEventUserRequest.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
            }
        }

        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(eventStatsHelper.getViewsCountForOneEvent(eventId));

        log.info("Событие ID: {} пользователя ID: {} успешно обновлено", eventId, userId);
        return eventFullDto;
    }

    private static void validateEventState(Event event) {
        if (event.getState() == State.PUBLISHED) {
            log.warn("Валидация изменения статуса провалена: событие ID: {} уже имеет статус PUBLISHED", event.getId());
            throw new ConditionsNotMetException("Only pending or canceled events can be changed");
        }
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsForInitiator(long userId, long eventId) {
//        userService.checkUserExists(userId);
        log.info("Получен запрос инициатора ID: {} на выгрузку заявок к событию ID: {}", userId, eventId);
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
        log.info("Получен запрос от инициатора ID: {} на изменение статуса заявок к событию ID: {}", userId, eventId);

        Event event = getOwnedEventById(userId, eventId);
        if (event.isParticipantLimitReached()) {
            log.warn("Изменение статуса заявок прервано: для события ID: {} достигнут лимит участников", eventId);
            throw new ConditionsNotMetException("The participant limit for this event has been reached.");
        }

        long availableSlots = event.getParticipantLimit() - event.getConfirmedRequests();
        log.debug("Событие ID: {}, доступно свободных слотов для подтверждения: {}", eventId, availableSlots);

        EventRequestStatusUpdateResult eventRequestStatusUpdateResult = requestService.changeRequestsStatusByOwnerEvent(
                statusUpdateRequest,
                availableSlots
        );

        List<ParticipationRequestDto> confirmedRequests = eventRequestStatusUpdateResult.getConfirmedRequests();

        int countConfirmedRequest = 0;

        while (countConfirmedRequest < confirmedRequests.size()) {
            event.incrementConfirmedRequests();
            countConfirmedRequest++;
        }
        log.info("Количество подтвержденных заявок {}", event.getConfirmedRequests());
        log.info("Успешно обновлены статусы заявок для события ID: {}. Число подтвержденных счетчиков увеличено на {}",
                eventId, countConfirmedRequest);
        return eventRequestStatusUpdateResult;
    }

    private Event getOwnedEventById(long userId, long eventId) {
        log.debug("Поиск события по ID: {} и инициатору ID: {}", eventId, userId);
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> {
                    log.warn("Событие ID: {} у инициатора ID: {} не найдено", eventId, userId);
                    return new NotFoundException(String.format("Event with ID %s was not found", eventId));
                });
    }

    private static void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            log.warn("Валидация даты провалена: дата события {} должна быть минимум на 2 часа позже текущего момента", eventDate);
            throw new ConditionsNotMetException(String.format("Field: eventDate. Error: must be at least 2 hours in" +
                    " the future from the current moment. Value: %s", eventDate));
        }
    }

    //    @Override
//    public void checkEventExists(long eventId) {
//        if (!eventRepository.existsById(eventId)) {
//            throw new NotFoundException(String.format("Event with ID %s not found", eventId));
//        }
//    }

}

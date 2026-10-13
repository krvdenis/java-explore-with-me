package ru.practicum.event.admin;

import com.querydsl.core.BooleanBuilder;
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
import ru.practicum.event.common.EventState;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.model.QEvent;
import ru.practicum.event.common.util.EventStatsHelper;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.util.Constants.START_OF_TIME;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminEventServiceImpl implements AdminEventService {
    private static final int MIN_HOURS_BEFORE_EVENT = 1;

    EventRepository eventRepository;
    PublicCategoryService categoryService;
    EventStatsHelper eventStatsHelper;

    @Override
    @Transactional
    public EventFullDto updateEvent(long id, UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Получен запрос администратора на обновление события с ID: {}", id);

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Событие с ID: {} не найдено", id);
                    return new NotFoundException(String.format("Event with ID %s was not found", id));
                });

        Long views = event.getState() == EventState.PUBLISHED ? eventStatsHelper.getViewsCountForOneEvent(event) : 0L;
        log.debug("Текущее состояние события ID: {}: {}. Количество просмотров: {}", id, event.getState(), views);

        if (updateEventAdminRequest.hasCategory()) {
            Category category = categoryService.getCategoryById(updateEventAdminRequest.getCategory());
            event.setCategory(category);
            log.debug("В событии ID: {} успешно обновлена категория", id);
        }

        LocalDateTime timeToPublish = LocalDateTime.now();

        validateRequest(updateEventAdminRequest, event, timeToPublish);

        AdminEventMapper.updateEventFields(event, updateEventAdminRequest);

        if (updateEventAdminRequest.hasStateAction()) {
            log.debug("Обработка административного действия со статусом: {}", updateEventAdminRequest.getStateAction());
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(timeToPublish);
                    log.info("Событие ID: {} успешно опубликовано в {}", id, timeToPublish);
                }
                case REJECT_EVENT -> {
                    event.setState(EventState.CANCELED);
                    log.info("Событие ID: {} отклонено администратором", id);
                }
            }
        }

        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(views);

        log.info("Обновление события ID: {} успешно завершено", id);
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getEvents(
            List<Long> users,
            List<EventState> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {
        log.info("Получен запрос администратора на поиск событий по фильтрам. Покупатели/Инициаторы: {}, Статусы: {}," +
                        " Категории: {}, Начало: {}, Конец: {}, Смещении от: {}, Размер страницы: {}",
                users, states, categories, rangeStart, rangeEnd, from, size);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("The start date of the search range must be before the end date.");
        }
        List<Long> filteredUsers = (users == null) ? List.of() : users.stream()
                .filter(Objects::nonNull)
                .toList();
        List<EventState> filteredStates = (states == null) ? List.of() : states.stream()
                .filter(Objects::nonNull)
                .toList();
        List<Long> filteredCategories = (categories == null) ? List.of() : categories.stream()
                .filter(Objects::nonNull)
                .toList();

        QEvent qEvent = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();
        buildQuery(users, rangeStart, rangeEnd, filteredUsers, builder, qEvent, filteredStates, filteredCategories);
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        log.debug("Сформирован QueryDSL builder поиска и пагинация: страница {}, размер {}", from / size, size);

        Map<Long, Event> eventMap = eventRepository.findAll(builder, pageable).getContent().stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

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

        List<EventFullDto> eventFullDtos = eventMap.values().stream()
                .map(event -> {
                    EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
                    ViewStatsDto viewStats = eventIdToViewStats.get(eventFullDto.getId());
                    Long views = (viewStats == null) ? 0L : viewStats.getHits();
                    eventFullDto.setViews(views);
                    return eventFullDto;
                })
                .toList();

        log.info("Сформирован и выгружен список из {} событий для администратора", eventFullDtos.size());
        return eventFullDtos;
    }

    @Override
    public boolean checkEventExistsByCategoryId(long categoryId) {
        log.debug("Проверка существования событий, привязанных к категории с ID: {}", categoryId);

        boolean exists = eventRepository.existsByCategoryId(categoryId);
        log.info("Результат проверки привязки событий к категории ID {}: {}", categoryId, exists);
        return exists;
    }

    @Override
    public Set<Event> getEvents(Set<Long> eventIds) {
        log.info("Получен запрос администратора на получение событий по их ID в количестве: {}", eventIds.size());
        Set<Event> events = new HashSet<>(eventRepository.findAllById(eventIds));

        log.info("Отправляется список из {} событий", events.size());
        return events;
    }

    private static void buildQuery(
            List<Long> users,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            List<Long> filteredUsers,
            BooleanBuilder builder,
            QEvent qEvent,
            List<EventState> filteredStates,
            List<Long> filteredCategories) {
        if (!filteredUsers.isEmpty()) {
            builder.and(qEvent.initiator.id.in(users));
        }

        if (!filteredStates.isEmpty()) {
            builder.and(qEvent.state.in(filteredStates));
        }

        if (!filteredCategories.isEmpty()) {
            builder.and(qEvent.category.id.in(filteredCategories));
        }


        if (rangeStart != null & rangeEnd != null) {
            builder.and(qEvent.eventDate.between(rangeStart, rangeEnd));
        }
    }

    private void validateRequest(
            UpdateEventAdminRequest request,
            Event event,
            LocalDateTime timeToPublish) {
        log.debug("Валидация административного запроса на обновление события ID: {}", event.getId());

        if (event.getEventDate().isBefore(timeToPublish.plusHours(MIN_HOURS_BEFORE_EVENT))) {
            log.warn("Валидация провалена: дата начала события ID {} ({}) слишком близко к моменту публикации ({})",
                    event.getId(), event.getEventDate(), timeToPublish);
            throw new ConditionsNotMetException("Cannot update the event because the event date must be at least" +
                    " one hour from now.");
        }

        if (request.getStateAction() == null) {
            return;
        }

        switch (request.getStateAction()) {
            case PUBLISH_EVENT -> {
                event.validateCanBePublished(timeToPublish);
            }
            case REJECT_EVENT -> event.validateCanBeRejected();
        }
        log.debug("Валидация административного запроса для события ID {} пройдена", event.getId());
    }
}

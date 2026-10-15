package ru.practicum.event.pub;

import com.querydsl.core.BooleanBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatsDto;
import ru.practicum.event.common.EventMapper;
import ru.practicum.event.common.EventRepository;
import ru.practicum.event.common.EventState;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.model.QEvent;
import ru.practicum.event.common.util.EventStatsHelper;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.util.Constants.START_OF_TIME;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicEventServiceImpl implements PublicEventService {
    EventRepository eventRepository;
    EventStatsHelper eventStatsHelper;

    @Override
    public EventFullDto getPublishedEventDtoById(long id) {
        log.info("Получен публичный запрос на получение события по ID: {}", id);

        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> {
                    log.warn("Опубликованное событие с ID: {} не найдено", id);
                    return new NotFoundException(String.format("Event with ID %s was not found", id));
                });
        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(eventStatsHelper.getViewsCountForOneEvent(event));

        log.info("Получена полная информация о событии с ID {}", event.getId());

        return eventFullDto;
    }

    @Override
    public Event getEventById(long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Событие с ID: {} не найдено", id);
                    return new NotFoundException(String.format("Event with ID %s was not found", id));
                });
        log.info("Получена информация о событии с ID {}", event);
        return event;
    }

    @Override
    public List<EventShortDto> getPublishedEvents(
            String text,
            List<Long> categories,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            boolean onlyAvailable,
            EventSort sort,
            int from,
            int size) {

        log.info("Получен публичный поисковый запрос событий. Параметры: text='{}', categories={}, paid={}," +
                        " rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("The start date of the search range must be before the end date.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> filteredCategories = (categories == null) ? List.of() : categories.stream()
                .filter(Objects::nonNull)
                .toList();

        QEvent qEvent = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();
        buildQuery(text, paid, rangeStart, rangeEnd, onlyAvailable, builder, qEvent, filteredCategories, now);
        log.info("Сформирован QueryDSL builder для поиска");

        List<Event> allEvents = new ArrayList<>(eventRepository.findAll(builder, Pageable.unpaged()).getContent());

        if (allEvents.isEmpty()) {
            log.info("Список найденных событий пуст");
            return List.of();
        }

        List<String> uris = allEvents.stream()
                .map(event -> String.format(EventStatsHelper.EVENT_URI_TEMPLATE, event.getId()))
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

        List<EventShortDto> eventShortDtos = allEvents.stream()
                .map(event -> {
                    EventShortDto eventShortDto = EventMapper.mapToShortDto(event);
                    ViewStatsDto viewStats = eventIdToViewStats.get(event.getId());
                    Long views = viewStats == null ? 0L : viewStats.getHits();
                    eventShortDto.setViews(views);
                    return eventShortDto;
                })
                .toList();

        Comparator<EventShortDto> comparator = Comparator.comparing(EventShortDto::getId);

        if (sort != null) {
            log.debug("Применение сортировки по типу: {}", sort);
            switch (sort) {
                case EVENT_DATE -> comparator = Comparator.comparing(EventShortDto::getEventDate);
                case VIEWS -> comparator = Comparator.comparing(EventShortDto::getViews).reversed();
            }
        }

        log.info("Возврат отсортированного списка с применением пагинации: from={}, size={}", from, size);

        return eventShortDtos.stream()
                .sorted(comparator)
                .skip(from)
                .limit(size)
                .toList();
    }

    private void buildQuery(
            String text,
            Boolean paid,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            boolean onlyAvailable,
            BooleanBuilder builder,
            QEvent qEvent,
            List<Long> filteredCategories,
            LocalDateTime now) {

        log.debug("Сборка динамического QueryDSL-запроса");

        if (text != null) {
            builder.and((qEvent.annotation.containsIgnoreCase(text)).or(qEvent.description.containsIgnoreCase(text)));
        }

        if (!filteredCategories.isEmpty()) {
            builder.and(qEvent.category.id.in(filteredCategories));
        }

        if (paid != null) {
            builder.and(qEvent.paid.eq(paid));
        }

        if (rangeStart != null && rangeEnd != null) {
            builder.and(qEvent.eventDate.between(rangeStart, rangeEnd));
        } else {
            builder.and(qEvent.eventDate.after(now));
        }

        if (onlyAvailable) {
            builder.and((qEvent.confirmedRequests.lt(qEvent.participantLimit)).or(qEvent.participantLimit.eq(0L)));
        }

        builder.and(qEvent.state.eq(EventState.PUBLISHED));
    }

}

package ru.practicum.event.pub;

import com.querydsl.core.BooleanBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatsDto;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShort;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.common.enums.State;
import ru.practicum.event.common.mapper.EventMapper;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.model.QEvent;
import ru.practicum.event.common.repository.EventRepository;
import ru.practicum.event.common.util.EventStatsHelper;
import ru.practicum.event.pub.enums.EventSort;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicEventServiceImpl implements PublicEventService {
    public static final State PUBLISHED = State.PUBLISHED;
    EventRepository eventRepository;
    EventStatsHelper eventStatsHelper;

    @Override
    public EventFullDto getPublishedEventDtoById(long eventId) {
        log.info("Получен публичный запрос на получение DTO события по ID: {}", eventId);

        Event event = getPublishedEventById(eventId);
        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(eventStatsHelper.getViewsCountForOneEvent(eventId));

        log.info("Успешно сформировано DTO для опубликованного события ID: {}", eventId);
        return eventFullDto;
    }

    @Override
    public Event getPublishedEventById(long eventId) {
        log.debug("Поиск опубликованного события по ID: {}", eventId);
        return eventRepository.findByIdAndState(eventId, PUBLISHED)
                .orElseThrow(() -> {
                    log.warn("Опубликованное событие с ID: {} не найдено", eventId);
                    return new NotFoundException(String.format("Event with ID %s was not found", eventId));
                });
    }

    @Override
    public Event getEventEntityById(long eventId) {
        log.debug("Поиск сущности события по ID: {}", eventId);
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.warn("Событие с ID: {} не найдено", eventId);
                    return new NotFoundException(String.format("Event with ID %s was not found", eventId));
                });
    }



    /*это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
    - текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
    - если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже
     текущей даты и времени
    - информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на
    участие*/

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

        log.info("Получен публичный поисковый запрос событий. Параметры: text='{}', categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        LocalDateTime now = LocalDateTime.now();
        List<Long> filteredCategories = (categories == null) ? List.of() : categories.stream()
                .filter(Objects::nonNull)
                .toList();

        QEvent qEvent = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();

        buildQuery(text, paid, rangeStart, rangeEnd, onlyAvailable, builder, qEvent, filteredCategories, now);

        Pageable pageable = pageable = PageRequest.of(from / size, size);
        log.debug("Сформирован QueryDSL builder для поиска");

        Map<Long, EventShort> eventMap = eventRepository.findAllShortBy(builder, pageable).getContent().stream()
                .collect(Collectors.toMap(EventShort::getId, Function.identity()));

        if (eventMap.isEmpty()) {
            log.info("Список найденных событий пуст");
            return List.of();
        }

        List<String> uris = eventMap.keySet().stream()
                .map(evenId -> String.format(EventStatsHelper.EVENT_URI_TEMPLATE, evenId))
                .toList();

        log.debug("Запрос статистики просмотров для {} URI", uris.size());
        Map<Long, ViewStatsDto> eventIdToViewStats = eventStatsHelper.getViewStatsDtos(
                        LocalDateTime.now().minusYears(5),
                        LocalDateTime.now().plusYears(5),
                        uris,
                        true
                ).stream()
                .collect(Collectors.toMap(viewStatsDto -> {
                            String[] uriParts = viewStatsDto.getUri().split("/");
                            Long eventid = Long.parseLong(uriParts[uriParts.length - 1]);
                            return eventid;
                        },
                        Function.identity()));

        List<EventShortDto> eventShortDtos = eventMap.values().stream()
                .map(event -> {
                    EventShortDto dto = EventMapper.mapToShortDto(event);
                    ViewStatsDto viewStats = eventIdToViewStats.get(event.getId());
                    Long views = viewStats == null ? 0L : viewStats.getHits();
                    dto.setViews(views);
                    return dto;
                })
                .toList();

        Comparator<EventShortDto> comparator = null;
        if (sort != null) {
            log.debug("Применение сортировки по типу: {}", sort);
            switch (sort) {
                case EVENT_DATE -> comparator = Comparator.comparing(EventShortDto::getEventDate);
                case VIEWS -> comparator = Comparator.comparing(EventShortDto::getViews).reversed();
            }
        }

        if (comparator == null) {
            log.info("Возврат несортированного списка из {} элементов", eventShortDtos.size());
            return eventShortDtos;
        }

        log.info("Возврат отсортированного списка из {} элементов", eventShortDtos.size());
        return eventShortDtos.stream()
                .sorted(comparator)
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
            builder.and(qEvent.annotation.containsIgnoreCase(text));
            builder.and((qEvent.description.containsIgnoreCase(text)));
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

        builder.and(qEvent.state.eq(State.PUBLISHED));
    }


//    @Override
//    public void checkEventExists(long eventId) {
//        if (!eventRepository.existsById(eventId)) {
//            throw new NotFoundException(String.format("Event with ID %s not found", eventId));
//        }
//    }

//    private void checkInitiator(long userId, long initiatorId) {
//        if (userId != initiatorId) {
//            throw new ConditionsNotMetException("Access denied: Only the creator can manage this resource.");
//        }
//    } ЕСЛИ НЕ ПОТРЕБУЕТСЯ УДАЛИТЬ

}

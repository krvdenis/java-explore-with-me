package ru.practicum.compilation.pub;

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
import ru.practicum.compilation.common.Compilation;
import ru.practicum.compilation.common.CompilationDto;
import ru.practicum.compilation.common.CompilationMapper;
import ru.practicum.compilation.common.CompilationRepository;
import ru.practicum.event.common.EventMapper;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.util.EventStatsHelper;
import ru.practicum.event.pub.PublicEventService;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.util.Constants.START_OF_TIME;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCompilationServiceImpl implements PublicCompilationService {
    PublicEventService eventService;
    CompilationRepository compilationRepository;
    EventStatsHelper eventStatsHelper;

    @Override
    public CompilationDto getCompilationById(long id) {
        log.info("Получен запрос на получение подборки с ID: {}", id);

        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка получения: подборка событий с ID {} не найдена", id);
                    return new NotFoundException(String.format("Compilation with ID %s was not found", id));
                });
        Set<Event> events = compilation.getEvents();
        log.debug("Для подборки ID: {} обнаружено {} событий в базе данных", id, events.size());

        Set<EventShortDto> eventShortDtos = new HashSet<>();

        if (!events.isEmpty()) {
            List<String> uris = events.stream()
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

            log.debug("Статистика получена. Сборка EventShortDto для подборки ID: {}", id);
            eventShortDtos = events.stream()
                    .map(event -> {
                        EventShortDto eventShortDto = EventMapper.mapToShortDto(event);
                        ViewStatsDto viewStats = eventIdToViewStats.get(eventShortDto.getId());
                        Long views = viewStats == null ? 0L : viewStats.getHits();
                        eventShortDto.setViews(views);
                        return eventShortDto;
                    })
                    .collect(Collectors.toSet());
        }

        CompilationDto compilationDto = CompilationMapper.mapToDto(compilation);
        compilationDto.setEvents(eventShortDtos);
        log.info("Подборка с ID: {} сформирована и готова к отправке", id);
        return compilationDto;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        log.info("Получен запрос на получение списка подборок. Фильтр pinned: {}, Пагинация: from={}, size={}",
                pinned, from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        List<Compilation> compilations = pinned != null ?
                compilationRepository.findAllByPinned(pinned, pageable).getContent() :
                compilationRepository.findAll(pageable).getContent();

        if (compilations.isEmpty()) {
            log.info("Список подборок пуст для заданных параметров");
            return List.of();
        }

        Set<Long> eventIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .map(Event::getId)
                .collect(Collectors.toSet());

        List<String> uris = eventIds.stream()
                .map(eventId -> String.format(EventStatsHelper.EVENT_URI_TEMPLATE, eventId))
                .toList();

        if (uris.isEmpty()) {
            log.info("В найденных подборках нет событий. Запрос к серверу статистики пропущен.");
            return compilations.stream()
                    .map(compilation -> {
                        CompilationDto compilationDto = CompilationMapper.mapToDto(compilation);
                        compilationDto.setEvents(Set.of());
                        return compilationDto;
                    }).collect(Collectors.toList());
        }

        log.info("Запрос статистики просмотров для {} уникальных URI из всех подборок", uris.size());
        Map<Long, ViewStatsDto> eventIdToViewStats = eventStatsHelper.getViewStatsDtos(
                        START_OF_TIME,
                        LocalDateTime.now(),
                        uris,
                        true
                ).stream()
                .collect(Collectors.toMap(viewStatsDto -> {
                            String[] uriParts = viewStatsDto.getUri().split("/");
                            return Long.parseLong(uriParts[uriParts.length - 1]);
                        },
                        Function.identity()));

        List<CompilationDto> result = compilations.stream()
                .map(compilation -> {
                    Set<EventShortDto> eventShortDtos = compilation.getEvents().stream()
                            .map(event -> {
                                ViewStatsDto viewStats = eventIdToViewStats.get(event.getId());
                                Long views = viewStats == null ? 0L : viewStats.getHits();
                                EventShortDto eventShortDto = EventMapper.mapToShortDto(event);
                                eventShortDto.setViews(views);
                                return eventShortDto;
                            })
                            .collect(Collectors.toSet());

                    CompilationDto compilationDto = CompilationMapper.mapToDto(compilation);
                    compilationDto.setEvents(eventShortDtos);
                    return compilationDto;
                }).collect(Collectors.toList());
        log.info("Получен список из {} подборок", result.size());
        return result;
    }
}

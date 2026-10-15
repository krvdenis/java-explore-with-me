package ru.practicum.compilation.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatsDto;
import ru.practicum.compilation.admin.dto.NewCompilationDto;
import ru.practicum.compilation.admin.dto.UpdateCompilationRequest;
import ru.practicum.compilation.common.Compilation;
import ru.practicum.compilation.common.CompilationDto;
import ru.practicum.compilation.common.CompilationMapper;
import ru.practicum.compilation.common.CompilationRepository;
import ru.practicum.event.admin.AdminEventService;
import ru.practicum.event.common.EventMapper;
import ru.practicum.event.common.dto.EventShortDto;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.util.EventStatsHelper;
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
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCompilationServiceImpl implements AdminCompilationService {
    CompilationRepository compilationRepository;
    AdminEventService eventService;
    EventStatsHelper eventStatsHelper;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        log.info("Получен запрос на создание подборки: {}", newCompilationDto);

        Compilation compilation = CompilationMapper.mapToCompilation(newCompilationDto);
        Set<Long> eventIds = newCompilationDto.getEvents();
        Set<EventShortDto> eventShortDtos = new HashSet<>();

        if (eventIds != null && !eventIds.isEmpty()) {
            Set<Event> events = eventService.getEvents(eventIds);

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
            compilation.getEvents().addAll(events);
        }

        compilationRepository.save(compilation);

        CompilationDto compilationDto = CompilationMapper.mapToDto(compilation);
        compilationDto.setEvents(eventShortDtos);
        log.info("Подборка успешно создана с ID: {}", compilationDto.getId());
        return compilationDto;
    }

    @Override
    public CompilationDto update(long id, UpdateCompilationRequest updateCompilationRequest) {
        log.info("Получен запрос на обновление подборки с ID: {}. Данные запроса: {}", id, updateCompilationRequest);

        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка получения: подборка событий с ID {} не найдена", id);
                    return new NotFoundException(String.format("Compilation with ID %s was not found", id));
                });
        Set<Event> events;
        Set<EventShortDto> eventShortDtos = new HashSet<>();

        if (updateCompilationRequest.hasEvents()) {
            compilation.getEvents().clear();

            Set<Long> eventIds = updateCompilationRequest.getEvents();
            events = eventIds.isEmpty() ? new HashSet<>() : eventService.getEvents(eventIds);

            compilation.getEvents().addAll(events);
        } else {
            events = compilation.getEvents();
        }

        CompilationMapper.updateCompilationFields(compilation, updateCompilationRequest);

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
        log.info("Подборка с ID: {} успешно обновлена", id);
        return compilationDto;
    }

    @Override
    public void delete(long id) {
        log.info("Получен запрос на удаление подборки с ID: {}", id);
        checkCompilationExists(id);

        log.info("Удаление данных подборки событий с ID {} из БД", id);
        compilationRepository.deleteById(id);
        log.info("Подборка событий с ID {} успешно удалена", id);
    }

    private void checkCompilationExists(long id) {
        log.info("Проверка существования подборки событий с ID: {}", id);

        if (!compilationRepository.existsById(id)) {
            log.warn("Ошибка проверки: подборка с ID {} не зарегистрирована в системе", id);
            throw new NotFoundException(String.format("Compilation with ID %s not found", id));
        }
    }
}

package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.mapper.EndpointHitMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final EndpointHitRepository repository;

    @Override
    @Transactional
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.info("Сохранение отметки о просмотре (hit) для URI: {}", endpointHitDto.getUri());
        EndpointHit endpointHit = EndpointHitMapper.mapToEndpointHit(endpointHitDto);
        EndpointHit savedHit = repository.save(endpointHit);
        log.debug("Хит успешно сохранен в БД с ID: {}", savedHit.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        validateDate(start, end);

        List<ViewStatsDto> result;

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                log.debug("Выборка статистики для всех URI с уникальными IP");
                result = repository.findStatsWithUniqueIp(start, end);
            } else {
                log.debug("Выборка статистики для всех URI со всеми IP");
                result = repository.findStatsWithAllIp(start, end);
            }
        } else {
            if (unique) {
                log.debug("Выборка статистики для списка URI ({}) с уникальными IP", uris.size());
                result = repository.findStatsWithUrisAndUniqueIp(start, end, uris);
            } else {
                log.debug("Выборка статистики для списка URI ({}) со всеми IP", uris.size());
                result = repository.findStatsWithUrisAndAllIp(start, end, uris);
            }
        }

        log.info("Сформирован отчет статистики. Найдено записей: {}", result.size());
        return result;
    }

    private void validateDate(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            log.warn("Валидация дат провалена: дата окончания {} раньше даты начала {}", end, start);
            throw new IllegalArgumentException("Дата \"end\" не может быть раньше даты \"start\"");
        }
    }
}
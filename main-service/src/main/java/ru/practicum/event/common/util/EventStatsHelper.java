package ru.practicum.event.common.util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.event.common.model.Event;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class EventStatsHelper {
    public static final String EVENT_URI_TEMPLATE = "/events/%d";
    private StatsClient statsClient;

    public long getViewsCountForOneEvent(Event event) {
        log.info("Запрос количества просмотров для события с ID: {}", event.getId());

        String eventUri = String.format(EVENT_URI_TEMPLATE, event.getId());
        List<ViewStatsDto> viewStatsDtos = getViewStatsDtos(
                event.getCreatedOn(),
                LocalDateTime.now(),
                List.of(eventUri),
                true
        );

        if (viewStatsDtos != null && !viewStatsDtos.isEmpty()) {
            long hits = viewStatsDtos.getFirst().getHits();
            log.info("Для события с ID: {} найдено просмотров: {}", event.getId(), hits);
            return hits;
        }

        log.info("Для события с ID: {} статистика просмотров отсутствует", event.getId());
        return 0L;
    }

    public List<ViewStatsDto> getViewStatsDtos(
            LocalDateTime start, LocalDateTime end, List<String> uris,
            boolean unique) {
        log.info("Запрос статистики просмотров. Параметры: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        ResponseEntity<List<ViewStatsDto>> response = statsClient.getStats(start, end, uris, unique);

        if (response == null || response.getBody() == null) {
            log.warn("Сервер статистики вернул пустой ответ (null) для URIs: {}", uris);
            return List.of();
        }

        List<ViewStatsDto> viewStatsDtos = response.getBody();
        log.info("Успешно получено {} элементов статистики от сервера", viewStatsDtos.size());
        return viewStatsDtos;
    }
}

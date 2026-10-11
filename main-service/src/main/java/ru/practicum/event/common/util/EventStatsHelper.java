package ru.practicum.event.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventStatsHelper {
    public static final String EVENT_URI_TEMPLATE = "/events/%d";
    private final StatsClient statsClient;

    public long getViewsCountForOneEvent(Long eventId) {
        String eventUri = String.format(EVENT_URI_TEMPLATE, eventId);
        List<ViewStatsDto> viewStatsDtos = getViewStatsDtos(
                LocalDateTime.now().minusYears(5),
                LocalDateTime.now().plusYears(5),
                List.of(eventUri),
                true
        );

        if (viewStatsDtos != null && !viewStatsDtos.isEmpty()) {
            return viewStatsDtos.getFirst().getHits();
        }
        return 0L;
    }

    public List<ViewStatsDto> getViewStatsDtos(LocalDateTime start, LocalDateTime end, List<String> uris,
                                                boolean unique) {
        List<ViewStatsDto> viewStatsDtos = statsClient.getStats(start, end, uris, unique).getBody();
        return viewStatsDtos;
    }
}

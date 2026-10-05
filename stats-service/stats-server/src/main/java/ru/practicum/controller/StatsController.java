package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class StatsController {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<Void> registerHit(@Valid @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Запрос на регистрацию информации об обработанном запросе сервиса ewm-main-service: {}",
                endpointHitDto);
        statsService.saveHit(endpointHitDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        log.info("Запрос на получение статистики: start: {}; end: {}; uris: {}; unique: {}", start, end, uris, unique);
        return ResponseEntity.ok(statsService.getStats(start, end, uris, unique));
    }
}
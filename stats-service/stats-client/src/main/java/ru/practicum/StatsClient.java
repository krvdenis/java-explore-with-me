package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class StatsClient {
    private static final String STATS_PATH = "/stats";
    private static final String HIT_PATH = "/hit";
    private static final String PARAM_START = "start";
    private static final String PARAM_END = "end";
    private static final String PARAM_URIS = "uris";
    private static final String PARAM_UNIQUE = "unique";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate rest;

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        log.info("Инициализация StatsClient. Базовый URL сервера статистики: {}", serverUrl);
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public void registerHit(EndpointHitDto endpointHitDto) {
        log.info("Отправка запроса на регистрацию хит-а (просмотра) для URI: {}", endpointHitDto.getUri());
        log.debug("Полные данные хит-а: {}", endpointHitDto);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHitDto, headers);

        try {
            ResponseEntity<Void> response = rest.exchange(HIT_PATH, HttpMethod.POST, requestEntity, Void.class);
            log.debug("Хит успешно зарегистрирован. Статус ответа сервера: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Ошибка при отправке хит-а на сервер статистики: {}", e.getMessage(), e);
            throw e;
        }
    }

    public ResponseEntity<List<ViewStatsDto>> getStats(LocalDateTime start,
                                                       LocalDateTime end,
                                                       List<String> uris,
                                                       boolean unique) {
        log.info("Запрос статистики за период с {} по {}", start, end);
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(STATS_PATH)
                .queryParam(PARAM_START, start.format(FORMATTER))
                .queryParam(PARAM_END, end.format(FORMATTER));

        if (uris != null && !uris.isEmpty()) {
            for (String uri : uris) {
                builder.queryParam(PARAM_URIS, uri);
            }
        }
        builder.queryParam(PARAM_UNIQUE, unique);

        String relativeUri = builder.build().toUriString();
        log.debug("Сформированный относительный URI для запроса статистики: {}", relativeUri);

        try {
            ResponseEntity<ViewStatsDto[]> response = rest.getForEntity(relativeUri, ViewStatsDto[].class);
            List<ViewStatsDto> body = response.getBody() != null ? List.of(response.getBody()) : List.of();

            log.info("Получен ответ от сервера статистики. Статус: {}. Количество элементов: {}",
                    response.getStatusCode(), body.size());
            log.debug("Данные ответа статистики: {}", body);
            return new ResponseEntity<>(body, response.getStatusCode());
        } catch (Exception e) {
            log.error("Ошибка при получении статистики от сервера: {}", e.getMessage(), e);
            throw e;
        }
    }
}
package ru.practicum.event.pub;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.dto.EventShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.util.Constants.PATH_EVENTS;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_EVENTS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicEventController {
    public static final String APP_NAME = "ewm-main-service";
    PublicEventService publicEventService;
    StatsClient statsClient;

    @GetMapping("/{id}")
    public EventFullDto getPublishedEventById(@PathVariable long id, HttpServletRequest request) {
        log.info("Запрос на получение информации об опубликованном событии с ID {}", id);
        registerHit(request);
        return publicEventService.getPublishedEventDtoById(id);
    }

    @GetMapping
    public List<EventShortDto> getPublishedEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) LocalDateTime rangeStart,
            @RequestParam(required = false) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") boolean onlyAvailable,
            @RequestParam(required = false) EventSort sort,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        log.info("Запрос на получение информации об опубликованных событиях");
        registerHit(request);
        return publicEventService.getPublishedEvents(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size
        );
    }

    private void registerHit(HttpServletRequest request) {
        log.info("Регистрация запроса к эндпоинту: path={}, client ip={}", request.getRequestURI(),
                request.getRemoteAddr());

        statsClient.registerHit(
                EndpointHitDto.builder()
                        .app(APP_NAME)
                        .uri(request.getRequestURI())
                        .ip(request.getRemoteAddr())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}

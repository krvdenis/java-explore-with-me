package ru.practicum.compilation.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.common.CompilationDto;

import java.util.List;

import static ru.practicum.util.Constants.PATH_COMPILATIONS;
import static ru.practicum.util.Constants.PATH_COMPILATION_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_COMPILATIONS)
public class PublicCompilationController {
    private final PublicCompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение информации о подборках: pinned - {}, from - {}, size - {}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping(PATH_COMPILATION_ID)
    public CompilationDto getCompilationById(@PathVariable long compId) {
        log.info("Запрос на получение информации о подборке с ID {}", compId);
        return compilationService.getCompilationById(compId);
    }

}

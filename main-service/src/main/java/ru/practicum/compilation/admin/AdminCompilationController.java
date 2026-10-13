package ru.practicum.compilation.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.compilation.admin.dto.NewCompilationDto;
import ru.practicum.compilation.admin.dto.UpdateCompilationRequest;
import ru.practicum.compilation.common.CompilationDto;

import static ru.practicum.util.Constants.PATH_ADMIN;
import static ru.practicum.util.Constants.PATH_COMPILATIONS;
import static ru.practicum.util.Constants.PATH_COMPILATION_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_ADMIN + PATH_COMPILATIONS)
public class AdminCompilationController {
    private final AdminCompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Запрос на создание подборки событий {}", newCompilationDto.getTitle());
        return compilationService.create(newCompilationDto);
    }

    @PatchMapping(PATH_COMPILATION_ID)
    public CompilationDto updateCompilation(
            @PathVariable long compId,
            @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("Запрос на обновление информации подборки событий с ID {}", compId);
        return compilationService.update(compId, updateCompilationRequest);
    }

    @DeleteMapping(PATH_COMPILATION_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilationById(@PathVariable long compId) {
        log.info("Запрос на удаление подборки событий с ID {}", compId);
        compilationService.delete(compId);
    }
}

package ru.practicum.compilation.pub;

import ru.practicum.compilation.common.CompilationDto;

import java.util.List;

public interface PublicCompilationService {
    CompilationDto getCompilationById(long id);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);
}

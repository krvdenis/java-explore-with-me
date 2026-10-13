package ru.practicum.compilation.admin;

import ru.practicum.compilation.admin.dto.NewCompilationDto;
import ru.practicum.compilation.admin.dto.UpdateCompilationRequest;
import ru.practicum.compilation.common.CompilationDto;

public interface AdminCompilationService {
    CompilationDto create(NewCompilationDto newCompilationDto);

    CompilationDto update(long id, UpdateCompilationRequest updateCompilationRequest);

    void delete(long id);
}

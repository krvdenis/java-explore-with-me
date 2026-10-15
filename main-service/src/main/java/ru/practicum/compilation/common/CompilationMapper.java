package ru.practicum.compilation.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.compilation.admin.dto.NewCompilationDto;
import ru.practicum.compilation.admin.dto.UpdateCompilationRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompilationMapper {
    public static Compilation mapToCompilation(NewCompilationDto newCompilationDto) {
        return Compilation.builder()
                .pinned(newCompilationDto.isPinned())
                .title(newCompilationDto.getTitle())
                .build();
    }

    public static CompilationDto mapToDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static void updateCompilationFields(Compilation compilation, UpdateCompilationRequest updateCompilation) {
        if (updateCompilation.hasPinned()) {
            compilation.setPinned(updateCompilation.getPinned());
        }

        if (updateCompilation.hasTitle()) {
            compilation.setTitle(updateCompilation.getTitle());
        }
    }
}

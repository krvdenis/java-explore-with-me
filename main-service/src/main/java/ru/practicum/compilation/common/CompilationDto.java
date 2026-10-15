package ru.practicum.compilation.common;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.common.dto.EventShortDto;

import java.util.Set;

@Builder
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {
    Long id;
    Set<EventShortDto> events;
    Boolean pinned;
    String title;
}

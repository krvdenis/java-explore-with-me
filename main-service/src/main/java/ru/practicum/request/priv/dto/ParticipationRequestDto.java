package ru.practicum.request.priv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.request.priv.RequestStatus;
import ru.practicum.util.Constants;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {
    Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    LocalDateTime created;

    Long event;
    Long requester;
    RequestStatus status;
}

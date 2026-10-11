package ru.practicum.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.request.enums.RequestStatus;
import ru.practicum.util.Constants;

import java.time.LocalDateTime;


@Builder
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    LocalDateTime created;
    Long event;
    Long id;
    Long requester;
    RequestStatus status;
}

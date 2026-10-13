package ru.practicum.request.priv.dto;

import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import ru.practicum.request.priv.RequestStatus;

import java.util.List;

@Value
@ToString
@NoArgsConstructor(force = true)
public class EventRequestStatusUpdateRequest {
    @NotNull
    List<Long> requestIds;

    @NotNull
    RequestStatus status;
}

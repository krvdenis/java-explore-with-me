package ru.practicum.request.priv;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.request.priv.dto.ParticipationRequestDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParticipationRequestMapper {
    public static ParticipationRequestDto mapToDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .id(request.getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}

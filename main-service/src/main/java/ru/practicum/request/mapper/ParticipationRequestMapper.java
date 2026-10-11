package ru.practicum.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

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

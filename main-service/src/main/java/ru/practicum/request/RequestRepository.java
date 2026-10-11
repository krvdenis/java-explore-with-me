package ru.practicum.request;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest ,Long> {
    boolean existsByRequesterIdAndEventId(long userId, long eventId);

    @EntityGraph(attributePaths = {"requester", "event"})
    List<ParticipationRequest> findAllByRequesterIdOrderByCreatedDesc(long userId);

    @EntityGraph(attributePaths = {"requester", "event"})
    List<ParticipationRequest> findAllByEventIdOrderByCreatedDesc(long eventId); //не уверен нужна ли тут сортировка, проверить на тестах

    @EntityGraph(attributePaths = {"requester", "event" })
    List<ParticipationRequest> findAllByIdInOrderByIdAsc(List<Long> requestIds);
}

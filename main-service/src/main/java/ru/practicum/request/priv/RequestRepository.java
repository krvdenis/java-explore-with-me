package ru.practicum.request.priv;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    boolean existsByRequesterIdAndEventId(long userId, long eventId);

    @EntityGraph(attributePaths = {"requester", "event"})
    List<ParticipationRequest> findAllByRequesterIdOrderByCreatedDesc(long userId);

    @EntityGraph(attributePaths = {"requester", "event"})
    List<ParticipationRequest> findAllByEventIdOrderByCreatedDesc(long eventId);

}

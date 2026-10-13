package ru.practicum.event.common;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.lang.NonNull;
import ru.practicum.event.common.dto.EventShort;
import ru.practicum.event.common.model.Event;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Page<EventShort> findAllByInitiatorId(long initiatorId, Pageable pageable);

    boolean existsByIdAndInitiatorId(long eventId, long initiatorId);

    Optional<Event> findByIdAndInitiatorId(long eventId, long initiatorId);

    Optional<Event> findByIdAndState(long eventId, EventState state);

    boolean existsByCategoryId(long categoryId);

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    @NonNull
    Page<Event> findAll(@NonNull Predicate predicate, @NonNull Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"category", "initiator"})
    @NonNull
    List<Event> findAllById(@NonNull Iterable<Long> longs);
}

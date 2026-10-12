package ru.practicum.event.common.repository;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.event.common.dto.EventShort;
import ru.practicum.event.common.enums.State;
import ru.practicum.event.common.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Page<EventShort> findAllByInitiatorId(long initiatorId, Pageable pageable);

    boolean existsByIdAndInitiatorId(long eventId, long initiatorId);

    Optional<Event> findByIdAndInitiatorId(long eventId, long initiatorId);

    Optional<Event> findByIdAndState(long eventId, State state);


    boolean existsByCategoryId(long categoryId);
}

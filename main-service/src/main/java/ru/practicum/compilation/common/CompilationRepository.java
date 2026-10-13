package ru.practicum.compilation.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Override
    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    @NonNull
    Optional<Compilation> findById(@NonNull Long id);


    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Page<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    @NonNull
    Page<Compilation> findAll(@NonNull Pageable pageable);
}

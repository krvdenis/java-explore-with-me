package ru.practicum.event.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.common.Category;
import ru.practicum.event.common.State;
import ru.practicum.event.common.model.embeddable.Location;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 2000)
    String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    Long confirmedRequests;

    @Column(nullable = false)
    LocalDateTime createdOn;

    @Column(nullable = false, length = 7000)
    String description;

    @Column(nullable = false)
    LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    User initiator;

    @Embedded
    Location location;

    @Column(nullable = false)
    Boolean paid;

    @Column(nullable = false)
    @PositiveOrZero
    Long participantLimit;

    LocalDateTime publishedOn;

    @Column(nullable = false)
    Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    State state;

    @Column(nullable = false)
    String title;

    public void validateCanBePublished(LocalDateTime timeToPublish) {
        if (this.state != State.PENDING) {
            throw new ConditionsNotMetException("Cannot publish the event because it is not in PENDING state.");
        }

    }

    public void validateCanBeRejected() {
        if (this.state == State.PUBLISHED) {
            throw new ConditionsNotMetException("Cannot reject the event because it is already PUBLISHED.");
        }
    }

    public boolean isParticipantLimitReached() {
        return this.participantLimit > 0
                && this.confirmedRequests >= this.participantLimit;
    }

    public void incrementConfirmedRequests() {
        if (this.confirmedRequests < this.participantLimit) {
            this.confirmedRequests++;
        }
    }
}
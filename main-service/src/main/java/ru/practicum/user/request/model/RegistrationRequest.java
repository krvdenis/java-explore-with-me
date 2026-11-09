package ru.practicum.user.request.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "registration_requests")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 254)
    String email;

    @Column(nullable = false, length = 250)
    String name;

    @Column(nullable = false, length = 100)
    String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    @Builder.Default()
    RegistrationStatus status = RegistrationStatus.PENDING;
}
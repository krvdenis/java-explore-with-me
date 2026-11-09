package ru.practicum.user.model;

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
@Table(name = "users")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true, length = 254)
    String email;

    @Column(nullable = false, length = 250)
    String name;

    @Column(nullable = false, length = 100)
    String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    UserRole role;

    public boolean isSimpleUser() {
        return this.role == UserRole.USER;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public boolean isSuperAdmin() {
        return this.role == UserRole.SUPER_ADMIN;
    }
}
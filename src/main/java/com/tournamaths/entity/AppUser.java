package com.tournamaths.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

// Calling this AppUser instead of User to avoid clashing with PostgreSQL-reserved word "user"
@Entity
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public @Data class AppUser {

    @Id
    @GeneratedValue
    private Long id;

    // Email is the user's "username" from the perspective of Spring Security
    @Column(nullable = false, unique = true)
    @NonNull
    private String email;

    @Column(nullable = false)
    @NonNull
    private String firstName;

    @Column(nullable = false)
    @NonNull
    private String lastName;

    @Column(nullable = false)
    @NonNull
    private String password;
}

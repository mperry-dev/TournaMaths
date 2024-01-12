package com.tournamaths.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public @Data class MathQuestion {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String identifier;

    @NonNull
    private String description;

    @NonNull
    private String equation;
}

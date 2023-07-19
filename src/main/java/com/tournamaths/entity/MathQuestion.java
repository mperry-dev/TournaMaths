package com.tournamaths.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class MathQuestion {

    @Id
    @GeneratedValue
    private Long id;
    private String question;

    // getters and setters
}

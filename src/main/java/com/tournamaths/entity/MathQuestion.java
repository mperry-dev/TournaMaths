package com.tournamaths.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class MathQuestion {

    @Id
    @GeneratedValue
    private Long id;
    private String question;

    // getters and setters
}

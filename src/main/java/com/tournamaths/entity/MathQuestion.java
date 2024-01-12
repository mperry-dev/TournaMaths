package com.tournamaths.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import lombok.Data;

@Entity
public @Data class MathQuestion {

    @Id
    @GeneratedValue
    private Long id;
    private String identifier;
    private String description;
    private String equation;

    public MathQuestion(){

    }

    public MathQuestion(String identifier, String description, String equation){
        this.identifier = identifier;
        this.description = description;
        this.equation = equation;
    }

    public String toString(){
        return id + ": " + identifier + " - " + description + " - " + equation;
    }

}

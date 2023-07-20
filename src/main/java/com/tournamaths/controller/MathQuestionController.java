package com.tournamaths.controller;

import com.tournamaths.entity.MathQuestion;
import com.tournamaths.repository.MathQuestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MathQuestionController {

    @Autowired
    private final MathQuestionRepository repository;

    public MathQuestionController(MathQuestionRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/questions")
    public MathQuestion newQuestion(@RequestBody MathQuestion newQuestion) {
        return repository.save(newQuestion);
    }
}

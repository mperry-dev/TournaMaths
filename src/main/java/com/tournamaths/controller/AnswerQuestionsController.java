package com.tournamaths.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnswerQuestionsController {
    @GetMapping("/answer_questions")
    public String home() {
        return "answer_question"; // This refers to 'answer_question.html' in 'src/main/resources/templates'
    }
}

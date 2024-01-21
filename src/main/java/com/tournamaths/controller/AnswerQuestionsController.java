package com.tournamaths.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnswerQuestionsController {
  @GetMapping("/answer_questions")
  public String home() {
    // This refers to 'answer_questions.html' in 'src/main/resources/templates'
    return "answer_questions";
  }
}

package com.tournamaths.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnswerQuestionsController {
  @GetMapping("/answer_questions")
  public String home() {
    return "answer_questions"; // This refers to 'answer_questions.html' in
                               // 'src/main/resources/templates'
  }
}

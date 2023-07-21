package com.tournamaths.controller;

import com.tournamaths.entity.MathQuestion;
import com.tournamaths.repository.MathQuestionRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
public class MathQuestionController {

    private final MathQuestionRepository repository;

    public MathQuestionController(MathQuestionRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/questions")
    public String newQuestion(@RequestParam String question, RedirectAttributes redirectAttributes) {
        MathQuestion newQuestion = new MathQuestion(question);
        repository.save(newQuestion);
        redirectAttributes.addFlashAttribute("message", "Question created successfully!");
        return "redirect:/questions";
    }

    @GetMapping("/questions")
    public String all() {
        Iterable<MathQuestion> questions = repository.findAll();

        StringBuilder html = new StringBuilder("<html><body>");

        // Add list of questions
        html.append("<h1>All Questions:</h1><ul>");
        for (MathQuestion question : questions) {
            html.append("<li>");
            html.append(question.toString());
            html.append("</li>");
        }
        html.append("</ul>");

        // Add form to create new question
        html.append("<h1>Add New Question:</h1>")
            .append("<form method='POST' action='/questions'>")
            .append("Question: <input type='text' name='question'><br>")
            .append("<input type='submit' value='Submit'>")
            .append("</form>");

        html.append("</body></html>");

        return html.toString();
    }
}

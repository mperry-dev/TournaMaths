package com.tournamaths.controller;

import com.tournamaths.entity.MathQuestion;
import com.tournamaths.repository.MathQuestionRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CreateQuestionsController {

    private final MathQuestionRepository repository;

    public CreateQuestionsController(MathQuestionRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/create_questions")
    public String newQuestion(@RequestParam String identifier, @RequestParam String description, @RequestParam String equation, RedirectAttributes redirectAttributes) {
        MathQuestion newQuestion = new MathQuestion(identifier, description, equation);
        repository.save(newQuestion);
        redirectAttributes.addFlashAttribute("message", "Question created successfully!");
        return "redirect:/create_questions";
    }

    @GetMapping("/create_questions")
    public String all(Model model) {
        Iterable<MathQuestion> questions = repository.findAll();
        model.addAttribute("questions", questions);
        return "create_questions";
    }
}

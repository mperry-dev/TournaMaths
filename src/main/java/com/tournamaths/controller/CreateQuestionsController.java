package com.tournamaths.controller;

import com.tournamaths.entity.MathQuestion;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CreateQuestionsController {

    @Autowired
    private SessionFactory sessionFactory;

    @PostMapping("/create_questions")
    public String newQuestion(@RequestParam String identifier, @RequestParam String description, @RequestParam String equation, RedirectAttributes redirectAttributes) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        MathQuestion newQuestion = new MathQuestion(identifier, description, equation);
        session.persist(newQuestion);
        session.getTransaction().commit();
        session.close();
        redirectAttributes.addFlashAttribute("message", "Question created successfully!");
        return "redirect:/create_questions";
    }

    @GetMapping("/create_questions")
    public String all(Model model) {
        Session session = sessionFactory.openSession();
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<MathQuestion> cq = cb.createQuery(MathQuestion.class);
        Root<MathQuestion> root = cq.from(MathQuestion.class);
        cq.select(root);
        Query<MathQuestion> query = session.createQuery(cq);
        List<MathQuestion> questions = query.getResultList();
        session.close();

        model.addAttribute("questions", questions);
        return "create_questions";
    }
}

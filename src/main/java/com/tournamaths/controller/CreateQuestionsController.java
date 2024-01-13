package com.tournamaths.controller;

import com.tournamaths.entity.MathQuestion;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Transactional means Spring handles committing/rolling back transaction, and opening/closing session automatically for public methods.
 * Default propagation is REQUIRED - if a transactional method calls another transactional method, the existing transaction continues.
 * It's important to this propagation approach since doing dependency injection of Session and CriteriaBuilder - want to avoid them being used in the wrong transaction.
 * Rolling back by default is only for Runtime exceptions (not checked exceptions).
 */
@Controller
@Transactional(rollbackFor = Exception.class) // rollback both after checked and unchecked exceptions
public class CreateQuestionsController {

    // Use Spring's dependency injection to injection session object, and tied to current transaction context.
    // Much nicer than having to Autowire and use SessionFactory!
    // Need to be careful not to use this outside of the transactional context,
    // otherwise it can be stale.
    @PersistenceContext
    private Session session;

    // Need to be careful not to use this outside of the transactional context,
    // otherwise it can be stale.
    @Autowired
    private CriteriaBuilder cb;

    @PostMapping("/create_questions")
    public String newQuestion(@RequestParam String identifier, @RequestParam String description, @RequestParam String equation, RedirectAttributes redirectAttributes) {
        MathQuestion newQuestion = new MathQuestion(identifier, description, equation);
        session.persist(newQuestion);
        redirectAttributes.addFlashAttribute("message", "Question created successfully!");
        return "redirect:/create_questions";
    }

    @GetMapping("/create_questions")
    public String all(Model model) {
        CriteriaQuery<MathQuestion> cq = cb.createQuery(MathQuestion.class);
        Root<MathQuestion> root = cq.from(MathQuestion.class);
        cq.select(root);
        Query<MathQuestion> query = session.createQuery(cq);
        List<MathQuestion> questions = query.getResultList();

        model.addAttribute("questions", questions);
        return "create_questions";
    }
}

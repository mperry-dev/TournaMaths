package com.tournamaths.controller;

import com.tournamaths.entity.MathQuestion;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
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
 * https://stackoverflow.com/questions/24710620/how-does-transactional-influence-current-session-in-hibernate
 * Default propagation is REQUIRED - if a transactional method calls another transactional method, the existing transaction continues.
 * It's important to this propagation approach since doing dependency injection of Session and CriteriaBuilder - want to avoid them being used in the wrong transaction.
 * Rolling back by default is only for Runtime exceptions (not checked exceptions).
 * NOTE because using Transactional, have container-managed transactions, so only need to rollback after unchecked exceptions https://docs.jboss.org/hibernate/orm/6.4/javadocs/org/hibernate/Session.html
 */
@Controller
@Transactional
public class CreateQuestionsController {

    // Use Spring's dependency injection to injection session object, and tied to current transaction context.
    // Much nicer than having to Autowire and use SessionFactory!
    // Need to be careful not to use this outside of the transactional context,
    // otherwise it can be stale.
    // Useful docs:
    // https://docs.jboss.org/hibernate/orm/4.0/devguide/en-US/html/ch02.html#d0e1198
    // https://www.baeldung.com/jpa-hibernate-persistence-context
    // https://stackoverflow.com/questions/31335211/autowired-vs-persistencecontext-for-entitymanager-bean
    // https://stackoverflow.com/questions/5640778/hibernate-sessionfactory-vs-jpa-entitymanagerfactory
    @PersistenceContext
    private Session session;

    // Need to be careful not to use this outside of the transactional context,
    // otherwise it can be stale.
    // Code shows how CriteriaBuilder reaches through SessionFactory when creating queries:
    // final SqmStatement<T> statement = sessionFactory.get().getQueryEngine().getHqlTranslator().translate( hql, resultClass );
    // https://github.com/hibernate/hibernate-orm/blob/dfa9cd5b2945f8384535853a54d836d121eb9067/hibernate-core/src/main/java/org/hibernate/query/sqm/internal/SqmCriteriaNodeBuilder.java#L320
    // SessionFactoryImpl.java -> queryEngine = QueryEngineImpl.from( this, bootMetamodel );
    @Autowired
    private HibernateCriteriaBuilder cb;

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

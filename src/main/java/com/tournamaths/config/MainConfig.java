package com.tournamaths.config;

import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.PersistenceContext;

@Configuration
@EnableTransactionManagement
public class MainConfig {
    /*
     * Need to be careful not to use this outside of the transactional context,
     * otherwise it can be stale.
     */

    // PersistenceContext used to inject Session/EntityManager
    // Note Session subclasses EntityManager - a Hibernate Session is an EntityManager https://docs.jboss.org/hibernate/orm/6.4/javadocs/org/hibernate/Session.html
    // If want thread-safety, need to use SessionFactory: https://www.baeldung.com/hibernate-entitymanager
    @PersistenceContext
    private Session session;

    @Bean
    public HibernateCriteriaBuilder criteriaBuilder() {
        return session.getCriteriaBuilder();
    }
}

package com.tournamaths.config;

import org.hibernate.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;

@Configuration
public class CriteriaBuilderConfig {
    /*
     * Need to be careful not to use this outside of the transactional context,
     * otherwise it can be stale.
     */

    @PersistenceContext
    private Session session;

    @Bean
    public CriteriaBuilder criteriaBuilder() {
        return session.getCriteriaBuilder();
    }
}

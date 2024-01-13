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
    // Useful info:
    // https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/Session.java
    // https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/SharedSessionContract.java
    // https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/internal/SessionImpl.java
    // https://github.com/hibernate/hibernate-orm/blob/main/hibernate-core/src/main/java/org/hibernate/engine/spi/SessionImplementor.java
    // https://github.com/hibernate/hibernate-orm/blob/dfa9cd5b2945f8384535853a54d836d121eb9067/hibernate-core/src/main/java/org/hibernate/engine/spi/SessionFactoryDelegatingImpl.java#L344
    // https://github.com/hibernate/hibernate-orm/blob/dfa9cd5b2945f8384535853a54d836d121eb9067/hibernate-core/src/main/java/org/hibernate/query/criteria/spi/HibernateCriteriaBuilderDelegate.java#L88
    @PersistenceContext
    private Session session;

    // https://www.baeldung.com/spring-bean
    @Bean
    public HibernateCriteriaBuilder criteriaBuilder() {
        System.out.println("Getting criteriaBuilder");
        return session.getCriteriaBuilder();
    }
}

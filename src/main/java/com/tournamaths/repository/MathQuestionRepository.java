package com.tournamaths.repository;

import com.tournamaths.entity.MathQuestion;

import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MathQuestionRepository extends JpaRepository<MathQuestion, Long> {
}

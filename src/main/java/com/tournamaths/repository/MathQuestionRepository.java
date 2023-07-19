package com.tournamaths.repository;

import com.tournamaths.entity.MathQuestion;
import org.springframework.data.repository.CrudRepository;

public interface MathQuestionRepository extends CrudRepository<MathQuestion, Long> {
}

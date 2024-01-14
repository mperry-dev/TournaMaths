package com.tournamaths.repository;

import com.tournamaths.entity.User;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmail(String email); // Spring Data JPA automatically implements this based on its name and signature
}

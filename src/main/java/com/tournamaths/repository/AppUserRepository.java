package com.tournamaths.repository;

import com.tournamaths.entity.AppUser;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface AppUserRepository extends CrudRepository<AppUser, Long> {
  // Spring Data JPA automatically implements this based on its name and signature
  // blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah blah 
  Optional<AppUser> findByEmail(String email);
}

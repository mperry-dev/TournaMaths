package com.tournamaths.service;

import com.tournamaths.entity.AppUser;
import com.tournamaths.repository.AppUserRepository;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TournaMathsUserDetailsService implements UserDetailsService {
  // This is used automatically by SpringBoot security to load a user, particularly for
  // AuthenticationManager.

  @Autowired private AppUserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    AppUser user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), Collections.emptyList());
  }
}

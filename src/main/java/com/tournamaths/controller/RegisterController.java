package com.tournamaths.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tournamaths.entity.AppUser;
import com.tournamaths.repository.AppUserRepository;

import jakarta.transaction.Transactional;

@RestController
@Transactional
public class RegisterController {
    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@ModelAttribute AppUser user) {
        String password = user.getPassword();

        // Add user to database
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Authenticate the user after registration
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getEmail(), password);
        Authentication authentication = authenticationManager.authenticate(authToken);

        // Set the authentication in the security context, including granted authorities and other details
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return ResponseEntity.ok("User registered and logged in successfully");
    }
}

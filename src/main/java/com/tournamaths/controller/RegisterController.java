package com.tournamaths.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.tournamaths.entity.AppUser;
import com.tournamaths.repository.AppUserRepository;

import jakarta.servlet.http.HttpSession;
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
    public RedirectView registerUser(@ModelAttribute AppUser user, HttpSession session) {
        // Check if the email is already in use - if so user can reattempt registration
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return new RedirectView("/?error");
        }

        String password = user.getPassword();

        // Add user to database
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Authenticate the user after registration
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getEmail(), password);
        Authentication authentication = authenticationManager.authenticate(authToken);

        // Set the authentication in the security context, including granted authorities and other details
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Set the authentication in the user's session
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        // Redirect to /create_questions page (RestController-specific way)
        return new RedirectView("/create_questions");
    }
}

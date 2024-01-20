package com.tournamaths.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If user is already logged in, redirect to home page.
        // https://stackoverflow.com/questions/12371770/spring-mvc-checking-if-user-is-already-logged-in-via-spring-security
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)){
            return "redirect:/";
        }

        // If user isn't logged in, serve login page.
        return "login";
    }
}

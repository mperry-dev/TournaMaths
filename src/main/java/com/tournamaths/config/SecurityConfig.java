package com.tournamaths.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity // https://spring.io/guides/gs/securing-web/
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true) // https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
public class SecurityConfig {
    // We can add lots of SecurityFilterChains in this method
    // https://medium.com/@2015-2-60-004/multiple-spring-security-configurations-form-based-token-based-authentication-c65ffbeabd07

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CSRF protection with a cookie-based CSRF token repository
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            // Enable XSS protection https://www.baeldung.com/spring-prevent-xss#making-an-application-xss-safewith-spring-security
            .headers(headers ->
                headers.xssProtection(
                        xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                ).contentSecurityPolicy(
                        cps -> cps.policyDirectives("script-src 'self'")
                ))
            // Configure other headers for security
            .headers(headers ->
                headers.frameOptions(Customizer.withDefaults())
                .contentTypeOptions(Customizer.withDefaults())
                .cacheControl(Customizer.withDefaults())
                .httpStrictTransportSecurity(Customizer.withDefaults())
            )
            // Define authorization rules - e.g. could enable any endpoint starting with /public if configured here.
            // Here enable any authenticated request to access endpoints
            .authorizeHttpRequests(auth ->
                auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()  // Permit static resources
                .requestMatchers("/", "/register", "/public/**").permitAll() // Allow anyone to access the home page, registration endpoint, or any page starting with /public/. Login page handled separately below.
                .anyRequest().authenticated() // All other requests must be authenticated
            )
            // Configure form login - sets up a page for users to login
            .formLogin(form -> form.loginPage("/login").permitAll()) //Springboot will use session-based authentication by default.
            .logout((logout) ->
                logout.permitAll()
                .deleteCookies("JSESSIONID") // https://www.baeldung.com/spring-security-login#3-configuration-for-form-login
                .logoutUrl("/logout") // TODO = call this
                .logoutSuccessUrl("/logout-success") // TODO = implement
            );

        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // For salting and hashing passwords.
        // BCrypt stores prefix, cost factor, salt and hash in resultant string.
        return new BCryptPasswordEncoder();
    }
}

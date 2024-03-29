package com.tournamaths.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.tournamaths.filter.IpAddressRateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity // https://spring.io/guides/gs/securing-web/
// https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
  @Autowired private IpAddressRateLimitingFilter loginAndRegistrationRateLimitingFilter;

  // We can add lots of SecurityFilterChains in this method (need to be careful though since the
  // first SecurityFilterChain matching an endpoint is used to the exclusion of later ones)
  // https://medium.com/@2015-2-60-004/multiple-spring-security-configurations-form-based-token-based-authentication-c65ffbeabd07

  @Bean
  public SecurityFilterChain mainFilterChain(HttpSecurity http) throws Exception {
    http
        // Enable CSRF protection with a cookie-based CSRF token repository
        // By default, HttpOnly flag is set to true, which means client-side scripts cannot read
        // CSRF token:
        // https://github.com/spring-projects/spring-security/blob/main/web/src/main/java/org/springframework/security/web/csrf/CookieCsrfTokenRepository.java#L58C2-L58C2
        .csrf(csrf -> csrf.csrfTokenRepository(new CookieCsrfTokenRepository()))
        // Enable XSS protection
        // https://www.baeldung.com/spring-prevent-xss#making-an-application-xss-safewith-spring-security
        .headers(
            headers ->
                headers
                    .xssProtection(
                        xss ->
                            xss.headerValue(
                                XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                    .contentSecurityPolicy(
                        cspc ->
                            // Allow only a restricted list of scripts and styles.
                            // upgrade-insecure-requests requires all resources to be loaded over
                            // HTTPS.
                            // frame-ancestors prevents clickjacking attacks.
                            // We only allow images from our server.
                            // We disallow object-src to prevent Flash/Java applets.
                            // Restricting base-uri helps mitigate phishing or redirection attacks.
                            // connect-src restricts AJAX, Websocket and other similar connections.
                            // form-action controls where forms can submit data.
                            // The font-src uses the cdnjs link as a prefix
                            // http://www.w3.org/TR/CSP/#match-source-expression
                            // As the CSP lists both URLs and hashes, it requires resources to match
                            // both 1 URL and 1 hash.
                            // For readability, I've listed each hash after its script URL.
                            cspc.policyDirectives(
                                "default-src 'none'; script-src 'self'"
                                    + " https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"
                                    + " 'sha384-1H217gwSVyLSIfaLxHbE7dRb3v4mYCKbpQvzx0cegeju1MVsGrX5xXxAvs/HgeFs'"
                                    + " https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.16.9/katex.min.js"
                                    + " 'sha384-XjKyOOlGwcjNTAIQHIpgOno0Hl1YQqzUOEleOLALmuqehneUG+vnGctmUb0ZY0l8';"
                                    + " style-src 'self'"
                                    + " https://cdn.jsdelivr.net/npm/@picocss/pico@1/css/pico.min.css"
                                    + " 'sha384-bnKrovjvRzFUSqtvDhPloRir5qWWcx0KhrlfLaR4RXO9IUC+zJBuvclXv/fSdVyk'"
                                    + " https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.16.9/katex.min.css"
                                    + " 'sha384-n8MVd4RsNIU0tAv4ct0nTaAbDJwPJzDEaqSD1odI+WdtXRGWt2kTvGFasHpSy3SV';"
                                    + " font-src 'self'"
                                    + " https://cdnjs.cloudflare.com/ajax/libs/KaTeX/0.16.9/fonts/;"
                                    + " frame-ancestors 'self'; img-src 'self'; connect-src 'self';"
                                    + " form-action 'self'; object-src 'none'; base-uri 'self';"
                                    + " upgrade-insecure-requests")))
        // Configure other headers for security
        // See https://docs.spring.io/spring-security/site/docs/4.2.x/reference/html/headers.html
        // NOTE CORS is disabled by default.
        .headers(
            headers ->
                headers
                    .cacheControl(withDefaults())
                    // This sets X-Content-Type-Options to nosniff
                    .contentTypeOptions(withDefaults())
                    .referrerPolicy(
                        referrer -> referrer.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    // HSTS - declares should only use HTTPS. 31536000 seconds = 1 year.
                    .httpStrictTransportSecurity(
                        hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000))
                    .frameOptions(fo -> fo.sameOrigin()) // X-Frame-Options
                    // Permissions Policy (previously Feature Policy)
                    .permissionsPolicy(policy -> policy.policy("geolocation=(self)")))
        // Rate-limiting. We can allow the previous configurations to run before this, since broad
        // DDOS protection provided by Web ACL at infrastructure level (see waf.tf)
        .addFilterBefore(
            loginAndRegistrationRateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
        // Define authorization rules - e.g. could enable any endpoint starting with /public if
        // configured here.
        // Here enable any authenticated request to access endpoints
        .authorizeHttpRequests(
            auth ->
                // Permit static resources
                auth.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                    .permitAll()
                    // Allow anyone to access the home page, login page, registration endpoint, any
                    // page starting with /public/, or health check.
                    .requestMatchers("/", "/login", "/public/**", "/health_check")
                    .permitAll()
                    // Cannot access the login processing endpoint or the registration endpoint if
                    // already logged in, but if not logged in can access it.
                    .requestMatchers("/process_login", "/register")
                    .anonymous()
                    // All other requests must be authenticated
                    .anyRequest()
                    .authenticated())
        // Configure form login - sets up a page for users to login
        .formLogin(
            form -> // Springboot will use session-based authentication by default.
                // When the user logs in, they will be redirected to whichever page they previously
                // were trying to access, OR the create_questions page
                form.loginPage("/login")
                    .loginProcessingUrl("/process_login")
                    .usernameParameter("email")
                    // Don't specify permitAll() here, so that have granular control - we make
                    // /process_login inaccessible to logged-in users, /login accessible to all
                    // users
                    .defaultSuccessUrl("/create_questions"))
        .logout(
            logout ->
                logout
                    // https://www.baeldung.com/spring-security-login#3-configuration-for-form-login
                    .permitAll()
                    .deleteCookies("JSESSIONID")
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/"));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    // For salting and hashing passwords.
    // BCrypt stores prefix, cost factor, salt and hash in resultant string.
    // Strength of 14 is significantly harder to crack than default of 10.
    return new BCryptPasswordEncoder(14);
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}

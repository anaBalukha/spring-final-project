package com.example.homework.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize / @PostAuthorize on methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF is disabled: this is a stateless REST API consumed by non-browser
            // clients (Postman, Swagger UI, mobile apps). HTTP Basic auth is used,
            // so browser-based CSRF attacks do not apply here.
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                    // home page redirects to swagger and is public
                    .requestMatchers("/").permitAll()
                // public auth endpoints
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // Swagger UI — always accessible
                .requestMatchers(
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/api-docs/**", "/v3/api-docs/**"
                ).permitAll()
                // public application metadata (localized welcome message)
                .requestMatchers("/api/info").permitAll()
                // Actuator: liveness/readiness probes are public, the rest is ADMIN-only
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                // H2 web console (enabled only in the dev profile)
                .requestMatchers("/h2-console/**").permitAll()
                // anyone can read students and courses
                .requestMatchers(HttpMethod.GET, "/api/students/**", "/api/courses/**").permitAll()
                // only ADMIN can delete
                .requestMatchers(HttpMethod.DELETE, "/api/students/**", "/api/courses/**").hasRole("ADMIN")
                // only ADMIN can list all users
                    .requestMatchers("/api/auth/users", "/api/auth/users/**").hasRole("ADMIN")
                // everything else requires at least authentication
                .anyRequest().authenticated()
            )
            // allow the H2 console to render inside its own frames (dev profile)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            .httpBasic(Customizer.withDefaults())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

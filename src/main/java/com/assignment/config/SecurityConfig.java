package com.assignment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.assignment.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                // ==============================
                // FRONTEND FILES
                // ==============================

                .requestMatchers(
                    "/",
                    "/index.html",
                    "/student.html",
                    "/staff.html",
                    "/css/**",
                    "/js/**"
                )
                .permitAll()


                // ==============================
                // AUTHENTICATION
                // ==============================

                .requestMatchers(
                    "/api/auth/register",
                    "/api/auth/login"
                )
                .permitAll()


                // ==============================
                // STAFF APIs
                // ==============================

                .requestMatchers(
                    "/api/staff/**"
                )
                .hasRole("STAFF")


                // ==============================
                // STUDENT APIs
                // ==============================

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/tickets"
                )
                .hasRole("STUDENT")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/tickets/my"
                )
                .hasRole("STUDENT")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/tickets/*/close"
                )
                .hasRole("STUDENT")


                // ==============================
                // OTHER TICKET APIs
                // ==============================

                .requestMatchers(
                    "/api/tickets/**"
                )
                .authenticated()


                // ==============================
                // EVERYTHING ELSE
                // ==============================

                .anyRequest()
                .authenticated()
            )

            // ==============================
            // JWT FILTER
            // ==============================

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
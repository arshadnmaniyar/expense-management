package org.example.expensecommand.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for dev profile.
 * Correlation ID and request/response logging filters are registered via WebConfig's FilterRegistrationBean.
 * This allows them to run before Spring Security's authentication checks.
 */
@Configuration
@Profile("dev")
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // allow OpenAPI and Swagger UI in dev
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                        // allow actuator endpoints in dev if needed
                        .requestMatchers("/actuator/**").permitAll()
                        // allow API endpoints in dev for testing - ALL METHODS
                        .requestMatchers("/api/expenses/**").permitAll()
                        // everything else requires authentication (or change to permitAll for local convenience)
                        .anyRequest().permitAll()  // Dev environment - allow all for testing
                )
                // simple form login for dev convenience (optional)
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }
}

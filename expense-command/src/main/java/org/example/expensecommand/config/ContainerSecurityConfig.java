package org.example.expensecommand.config;

import org.example.expensecommand.security.JwtValidationFilter;
import org.example.expensecommand.security.CustomAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for container profile.
 * Includes JWT validation filter for token-based authentication.
 */
@Configuration
@Profile("container")
public class ContainerSecurityConfig {

    @Value("${docs.basic.username:admin}")
    private String docsUser;

    @Value("${docs.basic.password:change-me}")
    private String docsPassword;

    private final JwtValidationFilter jwtValidationFilter;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public ContainerSecurityConfig(JwtValidationFilter jwtValidationFilter,
                                   CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtValidationFilter = jwtValidationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain containerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // allow health/info for monitoring without auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // protect docs endpoints in container
                        .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui.html", "/swagger-ui/**").authenticated()
                        // all other endpoints require authentication (validated by JWT filter)
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                // Add JWT validation filter before username/password authentication
                .addFilterBefore(jwtValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserDetails user = User.withUsername(docsUser)
                .password(encoder.encode(docsPassword))
                .roles("DOCS")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
}

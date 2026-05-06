package org.example.expensecommand.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * JWT validation filter for container profile.
 * This filter validates JWT tokens from Authorization header.
 * Only active when "container" profile is enabled.
 */
@Component
@Profile("container")
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtValidationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator/health",
            "/actuator/info"
    );

    @Value("${jwt.secret:your-secret-key-change-in-production}")
    private String jwtSecret;

    @Value("${jwt.issuer:http://localhost:8081/auth/realms/expense-management}")
    private String jwtIssuer;

    @Value("${jwt.validation.enabled:true}")
    private boolean jwtValidationEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        if (!jwtValidationEnabled || isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || authHeader.trim().isEmpty()) {
                LOGGER.warn("Missing Authorization header for request: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Missing Authorization header\"}");
                return;
            }

            if (!authHeader.startsWith(BEARER_PREFIX)) {
                LOGGER.warn("Invalid Authorization header format for request: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid Authorization header format\"}");
                return;
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            // Validate JWT token
            if (!validateJwtToken(token)) {
                LOGGER.warn("Invalid or expired JWT token for request: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid or expired JWT token\"}");
                return;
            }

            // Token is valid, proceed
            LOGGER.debug("JWT token validation successful");
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            LOGGER.error("Error in JWT validation", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error\"}");
        }
    }

    /**
     * Validates the JWT token.
     * In a production environment, use a library like jjwt or spring-security-oauth2
     * to properly validate JWT tokens including signature verification.
     */
    private boolean validateJwtToken(String token) {
        try {
            // Basic validation: check if token is not empty
            if (token == null || token.trim().isEmpty()) {
                return false;
            }

            // TODO: Implement proper JWT validation
            // This is a placeholder. In production, use:
            // 1. jjwt library to parse and validate JWT
            // 2. Verify signature using the public key from Keycloak
            // 3. Check expiration time
            // 4. Validate issuer and audience claims

            // For now, we'll do basic validation
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                LOGGER.debug("Invalid JWT format: expected 3 parts, got {}", parts.length);
                return false;
            }

            LOGGER.debug("JWT token format is valid");
            return true;

        } catch (Exception e) {
            LOGGER.error("Error validating JWT token", e);
            return false;
        }
    }

    /**
     * Checks if a path should be excluded from JWT validation.
     */
    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return isExcludedPath(request.getRequestURI());
    }
}


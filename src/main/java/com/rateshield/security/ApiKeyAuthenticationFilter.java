package com.rateshield.security;

import com.rateshield.entity.ApiKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyValidator apiKeyValidator;

    public ApiKeyAuthenticationFilter(
            ApiKeyValidator apiKeyValidator
    ) {
        this.apiKeyValidator = apiKeyValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            ApiKey validatedKey =
                    apiKeyValidator.validate(apiKey);

            String email =
                    validatedKey.getUser().getEmail();

            String role =
                    validatedKey.getUser()
                            .getRole()
                            .getName()
                            .name();

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(
                                new SimpleGrantedAuthority(
                                    "ROLE_" + role
                                )
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (IllegalArgumentException ex) {

            SecurityContextHolder.clearContext();

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}
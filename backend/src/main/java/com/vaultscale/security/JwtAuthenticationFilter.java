package com.vaultscale.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

a


@Component
@RequiredArgsConstructor


public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {


        // Get Authorization header
        String authHeader =
                request.getHeader("Authorization");

        // No JWT available, so continue normally
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }


        // Remove "Bearer " and keep only the JWT
        String token = authHeader.substring(7);

        try {
            // Get email stored inside the JWT
            String email =
                    jwtService.extractUsername(token);

            // Check that this request is not already authenticated
            boolean notAuthenticated =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication() == null;

            if (email != null && notAuthenticated) {

                // Load the real user from the database
                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(email);

                // Check JWT signature, email and expiration
                if (jwtService.isTokenValid(
                        token,
                        userDetails
                )) {
                    authenticateUser(
                            userDetails,
                            request
                    );
                }
            }

        } catch (Exception exception) {
            // Invalid, expired or damaged token
            // Leave the user unauthenticated
            SecurityContextHolder.clearContext();
        }

        // Continue to the next filter or controller
        filterChain.doFilter(request, response);
    }

    private void authenticateUser(
            UserDetails userDetails,
            HttpServletRequest request
    ) {
        // Create a Spring Security authentication object
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // Add request information, such as IP and session ID
        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        // Tell Spring Security:
        // "This user is authenticated for this request"
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}

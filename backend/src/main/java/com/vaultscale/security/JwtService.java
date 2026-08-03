package com.vaultscale.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    // Reads the secret from application.yml
    @Value("${app.jwt.secret}")
    private String secretKey;

    // Reads token lifetime from application.yml
    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    // Create JWT after successful login
    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()

                // Store user's email inside the token
                .subject(userDetails.getUsername())

                // Store token creation time
                .issuedAt(new Date())

                // Store token expiry time
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + jwtExpiration
                        )
                )

                // Sign token using our secret key
                .signWith(getSigningKey())

                // Convert token into a String
                .compact();
    }

    // Get user's email from JWT
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Check whether token is valid
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        try {
            // Parse and verify the token only once
            Claims claims = extractAllClaims(token);

            String tokenUsername = claims.getSubject();
            Date expiration = claims.getExpiration();

            return Objects.equals(
                        tokenUsername,
                        userDetails.getUsername()
                    )
                    && expiration.after(new Date())
                    && userDetails.isEnabled();

        } catch (
                JwtException |
                IllegalArgumentException exception
        ) {
            // Invalid signature, expired token,
            // malformed token, empty token, etc.
            return false;
        }
    }

    // Verify the signature and read the token payload
    private Claims extractAllClaims(String token) {

        return Jwts.parser()

                // Use the same key that created the token
                .verifyWith(getSigningKey())

                // Build the JWT parser
                .build()

                // Verify and parse the signed JWT
                .parseSignedClaims(token)

                // Return the payload/claims
                .getPayload();
    }

    // Convert Base64 secret text into a cryptographic key
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}

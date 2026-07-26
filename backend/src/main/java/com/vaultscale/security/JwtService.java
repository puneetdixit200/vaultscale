package com.vaultscale.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// @Service = a Spring-managed singleton — one instance shared across the entire app

@Service
public class JwtService {

    // These values come from application.yml — never hardcode secrets
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration; // in milliseconds

    // ─── Generate Token ──────────────────────────────────────────────────
    // Called after successful login. Creates and signs the JWT.
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())  // email goes into the token payload
                .setIssuedAt(new Date())                // when it was created
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))  // when it expires
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // sign with our secret
                .compact(); // build the token string
    }

    // ─── Validate Token ──────────────────────────────────────────────────
    // Called on every request. Checks: is the token valid? is it expired?
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Token is valid if: email matches AND token is not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Extract the email (subject) from the token payload
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract any field from the JWT claims
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // parseClaimsJws verifies the signature — throws exception if tampered
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Convert our plain string secret into a proper cryptographic key
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

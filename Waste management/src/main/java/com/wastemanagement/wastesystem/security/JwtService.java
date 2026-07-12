package com.wastemanagement.wastesystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for generating, parsing, and validating JSON Web Tokens (JWT).
 *
 * Token structure:
 * - Subject: the user's email (used as the unique username)
 * - Claim "role": the user's role (SUPER_ADMIN, WORKER, CITIZEN) for quick access
 *   without hitting the database on every request
 * - Issued-at and expiration timestamps controlled via application.properties
 *
 * The signing key is derived from a Base64-encoded secret defined in
 * application.properties (jwt.secret), decoded once and reused for all
 * signing/verification operations.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.ms}")
    private long jwtExpirationMs;

    /**
     * Generates a JWT for the given user, embedding their role as a custom claim.
     *
     * @param userDetails the authenticated user's details
     * @param role        the user's role (e.g. SUPER_ADMIN, WORKER, CITIZEN)
     * @return a signed JWT string
     */
    public String generateToken(UserDetails userDetails, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return buildToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracts the username (subject) embedded in the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the role claim embedded in the token.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Checks whether the token is valid for the given user:
     * the username matches and the token has not expired.
     */
    /**
     * Checks whether the token is valid for the given user: the username
     * matches, the token has not expired, AND the account is still
     * enabled (User.active). The enabled check matters specifically for
     * JWT-based requests: unlike login (which goes through
     * DaoAuthenticationProvider's own pre-authentication checks),
     * JwtAuthenticationFilter authenticates a request purely through this
     * method, so without explicitly checking isEnabled() here, a Super
     * Admin deactivating a user mid-session would have no effect on that
     * user's already-issued, still-unexpired JWT — it would keep working
     * on every protected endpoint until it naturally expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && userDetails.isEnabled();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
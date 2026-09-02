package com.hospital.patientmanagement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Generates, validates, and revokes JWT tokens.
 *
 * <p>Uses JJWT 0.12.x API with HMAC-SHA256 (HS256). The secret must be at
 * least 256 bits (32 bytes); the default in application.yml is 73 characters
 * = 584 bits.
 *
 * <p>Logout uses an in-memory blacklist. This is sufficient for a single-node
 * deployment. Replace with a Redis-backed store for multi-node clusters or
 * for tokens with very long expiry.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    // In-memory blacklist for logout / token revocation.
    // Entries are never pruned in this implementation; a TTL-based eviction
    // (e.g. Caffeine or Redis) should be added before going to production.
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    // ------------------------------------------------------------------ //
    //  Token generation
    // ------------------------------------------------------------------ //

    public String generateToken(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        return buildToken(principal.getUsername(), principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
    }

    public String generateToken(CustomUserDetails userDetails) {
        return buildToken(userDetails.getUsername(), userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")));
    }

    private String buildToken(String username, String roles) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // ------------------------------------------------------------------ //
    //  Token validation / extraction
    // ------------------------------------------------------------------ //

    public boolean validateToken(String token) {
        if (tokenBlacklist.contains(token)) {
            log.debug("Token is blacklisted (logged out).");
            return false;
        }
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Empty/null JWT: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getRolesFromToken(String token) {
        return getClaims(token).get("roles", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ------------------------------------------------------------------ //
    //  Blacklist (logout / revocation)
    // ------------------------------------------------------------------ //

    public void blacklistToken(String token) {
        tokenBlacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    // ------------------------------------------------------------------ //
    //  Key helper
    // ------------------------------------------------------------------ //

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

package com.sase.app.security;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.oauth2.jwt.Jwt;

import java.text.ParseException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Supabase {@code /auth/v1/user} ile oturum doğrulandıktan sonra roll ve diğer claim'ler için
 * access token'ı yalnızca parse eder (imza doğrulaması yapmaz).
 */
public final class UnverifiedSupabaseJwtParser {

    private UnverifiedSupabaseJwtParser() {
    }

    public static Jwt parse(String tokenValue) throws ParseException {
        SignedJWT sj = SignedJWT.parse(tokenValue);
        JWTClaimsSet cs = sj.getJWTClaimsSet();
        Instant issuedAt = cs.getIssueTime() != null ? cs.getIssueTime().toInstant() : null;
        Instant expiresAt = cs.getExpirationTime() != null ? cs.getExpirationTime().toInstant() : null;
        Map<String, Object> headers = new LinkedHashMap<>();
        sj.getHeader().toJSONObject().forEach((key, val) -> headers.put(key.toString(), val));

        Map<String, Object> claims = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : cs.getClaims().entrySet()) {
            claims.put(e.getKey(), e.getValue());
        }

        return new Jwt(tokenValue, issuedAt, expiresAt, headers, claims);
    }
}

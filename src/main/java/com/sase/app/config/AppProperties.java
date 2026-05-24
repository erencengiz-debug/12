package com.sase.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        SupabaseProps supabase,
        CookieProps cookie
) {

    /** {@code jwtSecret} — Supabase “Legacy JWT Secret” ile imzalanan HS256 token’lar için (opsiyonel). */
    public record SupabaseProps(
            String projectUrl,
            String anonKey,
            String jwtSecret
    ) {}

    public record CookieProps(
            boolean secure,
            String sameSite,
            String accessTokenName,
            String refreshTokenName
    ) {
        public String accessTokenName() {
            return accessTokenName != null ? accessTokenName : "sb-access-token";
        }
        public String refreshTokenName() {
            return refreshTokenName != null ? refreshTokenName : "sb-refresh-token";
        }
        public String sameSite() {
            return sameSite != null ? sameSite : "Lax";
        }
    }
}

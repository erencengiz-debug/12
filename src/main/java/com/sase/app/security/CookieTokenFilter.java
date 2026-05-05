package com.sase.app.security;

import com.sase.app.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieTokenFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final AppProperties appProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractTokenFromCookie(request);

        if (token != null) {
            try {
                Jwt jwt = jwtDecoder.decode(token);
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + extractRole(jwt))
                );
                JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                log.debug("Geçersiz JWT token: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                clearTokenCookie(response);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        String tokenName = appProperties.cookie().accessTokenName();
        return Arrays.stream(cookies)
                .filter(c -> tokenName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String extractRole(Jwt jwt) {
        Map<String, Object> appMeta = jwt.getClaim("app_metadata");
        if (appMeta != null && appMeta.containsKey("role")) {
            return appMeta.get("role").toString().toUpperCase();
        }
        return "USER";
    }

    private void clearTokenCookie(HttpServletResponse response) {
        String header = appProperties.cookie().accessTokenName()
                + "=; HttpOnly; Path=/; Max-Age=0; SameSite="
                + appProperties.cookie().sameSite();
        response.addHeader(HttpHeaders.SET_COOKIE, header);
    }
}

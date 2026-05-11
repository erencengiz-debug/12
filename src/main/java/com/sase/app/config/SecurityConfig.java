package com.sase.app.config;

import com.sase.app.security.CookieTokenFilter;
import com.sase.app.service.AuthService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.DefaultJWKSetCache;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AppProperties.class)
@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CookieTokenFilter cookieTokenFilter,
            AppProperties appProperties,
            AuthService authService
    ) throws Exception {
        http
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/error",
                                "/favicon.ico", "/static/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(cookieTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)   // profile cache dahil tüm session temizlenir
                        .clearAuthentication(true)
                        .addLogoutHandler((request, response, authentication) -> {
                            // Supabase oturumunu kapat
                            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                                String token = jwtAuth.getToken().getTokenValue();
                                authService.signOut(token);
                            }
                            // Cookie'yi sil
                            String cookieName = appProperties.cookie().accessTokenName();
                            String sameSite = appProperties.cookie().sameSite();
                            boolean secure = appProperties.cookie().secure();
                            String header = cookieName + "=; HttpOnly; Path=/; Max-Age=0; SameSite=" + sameSite
                                    + (secure ? "; Secure" : "");
                            response.addHeader(HttpHeaders.SET_COOKIE, header);
                        })
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                        .accessDeniedHandler((req, res, e) -> res.sendRedirect("/login"))
                );

        return http.build();
    }

    // Supabase ES256 JWT'lerini doğrular; JWKS 60 dk cache'lenir (varsayılan 5 dk)
    @Bean
    @Primary
    public JwtDecoder jwtDecoder(AppProperties props) throws Exception {
        String issuerUri = props.supabase().projectUrl() + "/auth/v1";
        String jwksUri = issuerUri + "/.well-known/jwks.json";

        var jwkSetCache = new DefaultJWKSetCache(60, 30, TimeUnit.MINUTES);
        var jwkSet = new RemoteJWKSet<SecurityContext>(new URL(jwksUri), null, jwkSetCache);
        var keySelector = new JWSVerificationKeySelector<SecurityContext>(JWSAlgorithm.ES256, jwkSet);

        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(keySelector);

        NimbusJwtDecoder decoder = new NimbusJwtDecoder(jwtProcessor);
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    @Bean
    public WebClient supabaseWebClient(AppProperties props) {
        return WebClient.builder()
                .baseUrl(props.supabase().projectUrl())
                .defaultHeader("apikey", props.supabase().anonKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

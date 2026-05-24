package com.sase.app.config;

import com.sase.app.security.CookieTokenFilter;
import com.sase.app.service.AuthService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StringUtils;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


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

    /**
     * Supabase JWT doğrulama: önce JWKS (ES256/asimetrik), başarısız olursa ve
     * {@code app.supabase.jwt-secret} tanımlıysa HS256 ile tekrar dener (legacy “JWT Secret” projelerinde
     * JWKS boştur; doğrulama “no matching key(s)” ile düşebilir).
     * JWKS isteği için {@link RestTemplate} zaman aşımları kullanılır.
     */
    @Bean
    @Primary
    public JwtDecoder jwtDecoder(AppProperties props) {
        JwtDecoder jwkDecoder = buildJwkDecoder(props);
        String legacySecret = props.supabase().jwtSecret();
        if (!StringUtils.hasText(legacySecret)) {
            return jwkDecoder;
        }
        JwtDecoder hmacDecoder = buildLegacyHmacJwtDecoder(props, legacySecret);
        return token -> {
            try {
                return jwkDecoder.decode(token);
            } catch (JwtException primary) {
                try {
                    return hmacDecoder.decode(token);
                } catch (JwtException ignored) {
                    throw primary;
                }
            }
        };
    }

    private static JwtDecoder buildJwkDecoder(AppProperties props) {
        String issuerUri = issuerUriFromProjectUrl(props.supabase().projectUrl());
        String jwksUri = issuerUri + "/.well-known/jwks.json";

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(20).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());
        RestTemplate jwksRestTemplate = new RestTemplate(factory);

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwksUri)
                .restOperations(jwksRestTemplate)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    private static JwtDecoder buildLegacyHmacJwtDecoder(AppProperties props, String jwtSecretPlain) {
        String issuerUri = issuerUriFromProjectUrl(props.supabase().projectUrl());
        SecretKey key = new SecretKeySpec(
                jwtSecretPlain.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        return decoder;
    }

    private static String issuerUriFromProjectUrl(String projectUrl) {
        if (projectUrl == null) {
            return "/auth/v1";
        }
        String base = projectUrl.stripTrailing();
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/auth/v1";
    }

    /**
     * Supabase çağrıları JDK HttpClient üzerinden (HTTP/1.1). Reactor-Netty ile bazı ortamlarda
     * oluşan {@code SocketException: Connection reset} sorununu önlemek için.
     */
    @Bean
    public WebClient supabaseWebClient(AppProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        return WebClient.builder()
                .clientConnector(new JdkClientHttpConnector(httpClient))
                .baseUrl(props.supabase().projectUrl())
                .defaultHeader("apikey", props.supabase().anonKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}

package com.sase.app.service;

import com.sase.app.dto.auth.SupabaseAuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient supabaseWebClient;

    public SupabaseAuthResponse signIn(String email, String password) {
        try {
            return supabaseWebClient.post()
                    .uri("/auth/v1/token?grant_type=password")
                    .bodyValue(Map.of("email", email, "password", password))
                    .retrieve()
                    .onStatus(
                            status -> status.value() == HttpStatus.BAD_REQUEST.value()
                                    || status.value() == HttpStatus.UNAUTHORIZED.value(),
                            resp -> resp.bodyToMono(String.class)
                                    .map(body -> new BadCredentialsException("E-posta veya şifre hatalı"))
                    )
                    .bodyToMono(SupabaseAuthResponse.class)
                    .block();

        } catch (BadCredentialsException e) {
            throw e;
        } catch (WebClientResponseException e) {
            log.warn("Supabase login hatası [{} {}]: {}", e.getStatusCode(), e.getStatusText(), e.getResponseBodyAsString());
            throw new BadCredentialsException("Giriş başarısız. Lütfen tekrar deneyin.");
        } catch (Exception e) {
            log.error("Supabase erişim hatası: {}", e.getMessage(), e);
            throw new RuntimeException("Kimlik doğrulama servisi geçici olarak kullanılamıyor.");
        }
    }

    public void signOut(String accessToken) {
        try {
            supabaseWebClient.post()
                    .uri("/auth/v1/logout")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            log.warn("Supabase oturum kapatma başarısız (yok sayılıyor): {}", e.getMessage());
        }
    }
}

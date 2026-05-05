package com.sase.app.controller.web;

import com.sase.app.config.AppProperties;
import com.sase.app.dto.auth.SupabaseAuthResponse;
import com.sase.app.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppProperties appProperties;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            Authentication authentication,
            Model model
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        if (error != null) {
            model.addAttribute("errorMsg", "E-posta veya şifre hatalı. Lütfen tekrar deneyin.");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletResponse response
    ) {
        try {
            SupabaseAuthResponse auth = authService.signIn(email, password);
            int maxAge = auth.expiresIn() != null ? auth.expiresIn() : 3600;
            setTokenCookie(response, auth.accessToken(), maxAge);
            log.info("Kullanıcı giriş yaptı: {}", email);
            return "redirect:/";
        } catch (Exception e) {
            log.warn("Giriş başarısız - {}: {}", email, e.getMessage());
            return "redirect:/login?error";
        }
    }

    private void setTokenCookie(HttpServletResponse response, String token, int maxAgeSeconds) {
        String header = appProperties.cookie().accessTokenName() + "=" + token
                + "; HttpOnly; Path=/; Max-Age=" + maxAgeSeconds
                + "; SameSite=" + appProperties.cookie().sameSite()
                + (appProperties.cookie().secure() ? "; Secure" : "");
        response.addHeader(HttpHeaders.SET_COOKIE, header);
    }

}

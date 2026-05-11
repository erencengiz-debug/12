package com.sase.app.web.advice;

import com.sase.app.dto.profile.ProfileDto;
import com.sase.app.mapper.ProfileMapper;
import com.sase.app.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

/**
 * Her Thymeleaf view'a ${currentUser} (ProfileDto) değişkenini otomatik ekler.
 *
 * Profile, HTTP session'da cache'lenir; her request'te DB sorgusunu önler.
 * Oturum kapatıldığında Spring Security session'ı geçersiz kılar → cache temizlenir.
 * Profile yoksa null döner — template'ler null-safe th:if ile kontrol etmeli.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserAdvice {

    private static final String CACHE_KEY_PREFIX = "profile::";

    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @ModelAttribute("currentUser")
    public ProfileDto currentUser(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request) {

        if (jwt == null) return null;

        String userId = jwt.getSubject();
        String cacheKey = CACHE_KEY_PREFIX + userId;

        // Önce mevcut session'dan kontrol et (oluşturma)
        HttpSession session = request.getSession(false);
        if (session != null) {
            ProfileDto cached = (ProfileDto) session.getAttribute(cacheKey);
            if (cached != null) return cached;
        }

        // DB'den yükle ve session'a kaydet
        ProfileDto profile = profileService.findById(UUID.fromString(userId))
                .map(profileMapper::toDto)
                .orElse(null);

        if (profile != null) {
            request.getSession(true).setAttribute(cacheKey, profile);
        }
        return profile;
    }
}

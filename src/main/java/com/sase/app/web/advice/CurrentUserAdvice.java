package com.sase.app.web.advice;

import com.sase.app.dto.profile.ProfileDto;
import com.sase.app.mapper.ProfileMapper;
import com.sase.app.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

/**
 * Her Thymeleaf view'a ${currentUser} (ProfileDto) değişkenini otomatik ekler.
 * Profile yoksa null döner — template'ler null-safe th:if ile kontrol etmeli.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class CurrentUserAdvice {

    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    @ModelAttribute("currentUser")
    public ProfileDto currentUser(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) return null;
        UUID userId = UUID.fromString(jwt.getSubject());
        return profileService.findById(userId)
                .map(profileMapper::toDto)
                .orElse(null);
    }
}

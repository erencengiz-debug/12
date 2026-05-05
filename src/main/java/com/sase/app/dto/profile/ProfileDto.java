package com.sase.app.dto.profile;

import com.sase.app.entity.UserRole;

import java.util.UUID;

public record ProfileDto(
        UUID id,
        String kullaniciAdi,
        String adSoyad,
        UserRole role,
        Boolean aktif
) {
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isYonetici() {
        return role == UserRole.ADMIN || role == UserRole.YONETICI;
    }

    public String displayName() {
        if (adSoyad != null && !adSoyad.isBlank()) return adSoyad;
        return kullaniciAdi;
    }
}

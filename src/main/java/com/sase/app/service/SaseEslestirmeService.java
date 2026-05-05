package com.sase.app.service;

import com.sase.app.entity.SaseEslestirme;
import com.sase.app.repository.SaseEslestirmeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaseEslestirmeService {

    private final SaseEslestirmeRepository saseEslestirmeRepository;

    public List<SaseEslestirme> kullaniciyaAit(UUID userId) {
        return saseEslestirmeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public SaseEslestirme idIleGetir(UUID id) {
        return saseEslestirmeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Şase eşleştirme kaydı bulunamadı: " + id));
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public SaseEslestirme kaydet(SaseEslestirme eslestirme) {
        return saseEslestirmeRepository.save(eslestirme);
    }

    /**
     * Eşleştirmeyi çalıştırılmış (executed) olarak işaretle.
     * Gerçek eşleştirme mantığı ilerleyen aşamada buraya eklenir.
     */
    @Transactional
    public SaseEslestirme calistir(UUID id) {
        SaseEslestirme eslestirme = idIleGetir(id);
        eslestirme.setExecuted(true);
        eslestirme.setExecutedDate(OffsetDateTime.now());
        log.info("Şase eşleştirme çalıştırıldı: id={}, başlık={}", id, eslestirme.getBaslik());
        return saseEslestirmeRepository.save(eslestirme);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void sil(UUID id) {
        saseEslestirmeRepository.deleteById(id);
    }
}

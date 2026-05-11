package com.sase.app.service;

import com.sase.app.entity.Stok;
import com.sase.app.repository.StokRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StokService {

    private static final int PAGE_SIZE = 50;

    private final StokRepository stokRepository;

    public Page<Stok> listele(String q, int page) {
        if (q != null && !q.isBlank()) {
            // search sorgusu ORDER BY içermiyor → sort pageable'a verilir
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "stokAdi"));
            return stokRepository.search(q.trim(), pageable);
        }
        // findAllPaged sorgusu zaten ORDER BY stokAdi içeriyor → sort verilmez
        return stokRepository.findAllPaged(PageRequest.of(page, PAGE_SIZE));
    }

    public Stok idIleGetir(UUID id) {
        return stokRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stok bulunamadı: " + id));
    }

    public Stok detayGetir(UUID id) {
        return stokRepository.findDetailById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stok bulunamadı: " + id));
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'YONETICI')")
    public Stok kaydet(Stok stok) {
        return stokRepository.save(stok);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void sil(UUID id) {
        stokRepository.deleteById(id);
    }
}

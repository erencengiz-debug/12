package com.sase.app.service;

import com.sase.app.entity.Stok;
import com.sase.app.repository.StokRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("stokAdi").ascending());
        if (q != null && !q.isBlank()) {
            return stokRepository.search(q.trim(), pageable);
        }
        return stokRepository.findAllPaged(pageable);
    }

    public Stok idIleGetir(UUID id) {
        return stokRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stok bulunamadı: " + id));
    }

    @Transactional
    public Stok kaydet(Stok stok) {
        return stokRepository.save(stok);
    }

    @Transactional
    public void sil(UUID id) {
        stokRepository.deleteById(id);
    }
}

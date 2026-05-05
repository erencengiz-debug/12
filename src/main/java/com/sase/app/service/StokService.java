package com.sase.app.service;

import com.sase.app.entity.Stok;
import com.sase.app.repository.StokRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StokService {

    private final StokRepository stokRepository;

    public List<Stok> hepsiniGetir() {
        return stokRepository.findAll();
    }

    public List<Stok> aktifStoklar() {
        return stokRepository.findByStokStatusTrue();
    }

    public Stok idIleGetir(UUID id) {
        return stokRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Stok bulunamadı: " + id));
    }

    public List<Stok> ara(String q) {
        if (q == null || q.isBlank()) return stokRepository.findAll();
        return stokRepository.search(q.trim());
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

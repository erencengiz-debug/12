package com.sase.app.service;

import com.sase.app.dto.stok.StokOzetDto;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StokService {

    private static final int STOK_POPUP_SEARCH_CAP = 100;

    private final StokRepository stokRepository;

    public List<StokOzetDto> stokOzetiAra(String aramaMetni, int limit) {
        String q = aramaMetni == null ? "" : aramaMetni.trim();
        if (q.length() < 1) {
            return List.of();
        }
        int lim = Math.max(1, Math.min(limit, STOK_POPUP_SEARCH_CAP));
        Pageable pg = PageRequest.of(0, lim, Sort.by(Sort.Direction.ASC, "stokAdi"));
        Page<Stok> page = stokRepository.search(q, pg);
        return page.getContent().stream()
                .map(s -> new StokOzetDto(s.getId(), s.getStokKodu(), s.getStokAdi()))
                .toList();
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

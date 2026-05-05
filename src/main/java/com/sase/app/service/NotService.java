package com.sase.app.service;

import com.sase.app.entity.Not;
import com.sase.app.repository.NotRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotService {

    private final NotRepository notRepository;

    public List<Not> kullaniciyaAit(UUID userId) {
        return notRepository.findByUserIdOrderByTarihDesc(userId);
    }

    public Not idIleGetir(UUID id) {
        return notRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not bulunamadı: " + id));
    }

    @Transactional
    public Not kaydet(Not not) {
        return notRepository.save(not);
    }

    @Transactional
    public void sil(UUID id) {
        notRepository.deleteById(id);
    }
}

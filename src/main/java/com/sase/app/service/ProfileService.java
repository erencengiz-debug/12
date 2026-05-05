package com.sase.app.service;

import com.sase.app.entity.Profile;
import com.sase.app.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    public Optional<Profile> findById(UUID id) {
        return profileRepository.findById(id);
    }

    public Optional<Profile> findByKullaniciAdi(String kullaniciAdi) {
        return profileRepository.findByKullaniciAdi(kullaniciAdi);
    }

    @Transactional
    public Profile kaydet(Profile profile) {
        return profileRepository.save(profile);
    }
}

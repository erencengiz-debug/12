package com.sase.app.repository;

import com.sase.app.entity.SaseEslestirme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SaseEslestirmeRepository extends JpaRepository<SaseEslestirme, UUID> {

    List<SaseEslestirme> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<SaseEslestirme> findByExecutedFalseOrExecutedIsNull();

    long countByUserId(UUID userId);

    long countByUserIdAndExecutedTrue(UUID userId);
}

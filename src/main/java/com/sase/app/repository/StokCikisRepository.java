package com.sase.app.repository;

import com.sase.app.entity.StokCikis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StokCikisRepository extends JpaRepository<StokCikis, UUID> {

    List<StokCikis> findByStokIdOrderByCikisTarihiDesc(UUID stokId);
}

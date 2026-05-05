package com.sase.app.repository;

import com.sase.app.entity.StokFotograf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StokFotografRepository extends JpaRepository<StokFotograf, UUID> {

    List<StokFotograf> findByStokIdOrderBySiraAsc(UUID stokId);
}

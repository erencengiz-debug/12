package com.sase.app.repository;

import com.sase.app.entity.StokAlis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StokAlisRepository extends JpaRepository<StokAlis, UUID> {

    List<StokAlis> findByStokIdOrderByAlisTarihiDesc(UUID stokId);
}

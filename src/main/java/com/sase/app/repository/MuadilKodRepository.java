package com.sase.app.repository;

import com.sase.app.entity.MuadilKod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MuadilKodRepository extends JpaRepository<MuadilKod, UUID> {

    List<MuadilKod> findByStokId(UUID stokId);

    boolean existsByMuadilKod(String muadilKod);
}

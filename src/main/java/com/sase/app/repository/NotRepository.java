package com.sase.app.repository;

import com.sase.app.entity.Not;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotRepository extends JpaRepository<Not, UUID> {

    List<Not> findByUserIdOrderByTarihDesc(UUID userId);

    long countByUserId(UUID userId);
}

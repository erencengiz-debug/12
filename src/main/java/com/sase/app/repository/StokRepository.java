package com.sase.app.repository;

import com.sase.app.entity.Stok;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StokRepository extends JpaRepository<Stok, UUID> {

    Optional<Stok> findByStokKodu(String stokKodu);

    List<Stok> findByMarkaIgnoreCase(String marka);

    Page<Stok> findByStokStatusTrue(Pageable pageable);

    @Query("SELECT s FROM Stok s WHERE " +
           "LOWER(s.stokAdi) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.stokKodu) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.orjKodu) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Stok> search(@Param("q") String q, Pageable pageable);

    @Query("SELECT s FROM Stok s ORDER BY s.stokAdi ASC")
    Page<Stok> findAllPaged(Pageable pageable);
}

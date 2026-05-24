package com.sase.app.repository;

import com.sase.app.entity.Stok;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StokRepository extends JpaRepository<Stok, UUID> {

    @EntityGraph(attributePaths = {"muadilKodlar", "fotograflar", "alisList", "cikisList"})
    @Query("SELECT s FROM Stok s WHERE s.id = :id")
    Optional<Stok> findDetailById(@Param("id") UUID id);

    Optional<Stok> findByStokKodu(String stokKodu);

    List<Stok> findByMarkaIgnoreCase(String marka);

    Page<Stok> findByStokStatusTrue(Pageable pageable);

    @Query("SELECT s FROM Stok s WHERE " +
           "LOWER(s.stokAdi) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.stokKodu) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.orjKodu) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Stok> search(@Param("q") String q, Pageable pageable);

    /** Manuel /sase araç seçimi — cascade filtre seçenekleri (PostgreSQL) */
    @Query(value = """
            SELECT DISTINCT TRIM(s.model_kodu) AS mk
            FROM stoklar s
            WHERE s.model_kodu IS NOT NULL
              AND LENGTH(TRIM(s.model_kodu)) > 0
              AND LOWER(TRIM(s.marka)) IN (:markalar)
            ORDER BY TRIM(s.model_kodu)
            LIMIT 400
            """, nativeQuery = true)
    List<String> manuelDistinctModelKodu(@Param("markalar") List<String> markalar);

    @Query(value = """
            SELECT DISTINCT TRIM(s.kisa_ismi) AS kk
            FROM stoklar s
            WHERE s.kisa_ismi IS NOT NULL
              AND LENGTH(TRIM(s.kisa_ismi)) > 0
              AND LOWER(TRIM(s.marka)) IN (:markalar)
              AND TRIM(s.model_kodu) = TRIM(:model)
            ORDER BY TRIM(s.kisa_ismi)
            LIMIT 400
            """, nativeQuery = true)
    List<String> manuelDistinctKisaIsmi(
            @Param("markalar") List<String> markalar,
            @Param("model") String model
    );

    @Query(value = """
            SELECT DISTINCT TRIM(s.kategori_1) AS k1
            FROM stoklar s
            WHERE s.kategori_1 IS NOT NULL
              AND LENGTH(TRIM(s.kategori_1)) > 0
              AND LOWER(TRIM(s.marka)) IN (:markalar)
              AND TRIM(s.model_kodu) = TRIM(:model)
              AND (
                    LENGTH(TRIM(CAST(:kisa AS VARCHAR))) = 0
                    OR TRIM(s.kisa_ismi) = TRIM(CAST(:kisa AS VARCHAR))
              )
            ORDER BY TRIM(s.kategori_1)
            LIMIT 400
            """, nativeQuery = true)
    List<String> manuelDistinctKategori1(
            @Param("markalar") List<String> markalar,
            @Param("model") String model,
            @Param("kisa") String kisa
    );

    @Query(value = """
            SELECT DISTINCT TRIM(s.kategori_2) AS k2
            FROM stoklar s
            WHERE s.kategori_2 IS NOT NULL
              AND LENGTH(TRIM(s.kategori_2)) > 0
              AND LOWER(TRIM(s.marka)) IN (:markalar)
              AND TRIM(s.model_kodu) = TRIM(:model)
              AND (
                    LENGTH(TRIM(CAST(:kisa AS VARCHAR))) = 0
                    OR TRIM(s.kisa_ismi) = TRIM(CAST(:kisa AS VARCHAR))
              )
              AND TRIM(s.kategori_1) = TRIM(CAST(:kat1 AS VARCHAR))
            ORDER BY TRIM(s.kategori_2)
            LIMIT 400
            """, nativeQuery = true)
    List<String> manuelDistinctKategori2(
            @Param("markalar") List<String> markalar,
            @Param("model") String model,
            @Param("kisa") String kisa,
            @Param("kat1") String kat1
    );

    @Query(value = """
            SELECT DISTINCT TRIM(COALESCE(s.alternatif_stok_adi, '')) AS a1
            FROM stoklar s
            WHERE s.alternatif_stok_adi IS NOT NULL
              AND LENGTH(TRIM(COALESCE(s.alternatif_stok_adi, ''))) > 0
              AND LOWER(TRIM(s.marka)) IN (:markalar)
              AND TRIM(s.model_kodu) = TRIM(:model)
              AND (
                    LENGTH(TRIM(CAST(:kisa AS VARCHAR))) = 0
                    OR TRIM(s.kisa_ismi) = TRIM(CAST(:kisa AS VARCHAR))
              )
              AND TRIM(s.kategori_1) = TRIM(CAST(:kat1 AS VARCHAR))
              AND TRIM(s.kategori_2) = TRIM(CAST(:kat2 AS VARCHAR))
            ORDER BY TRIM(COALESCE(s.alternatif_stok_adi, ''))
            LIMIT 350
            """, nativeQuery = true)
    List<String> manuelDistinctAlternatifStokAdi(
            @Param("markalar") List<String> markalar,
            @Param("model") String model,
            @Param("kisa") String kisa,
            @Param("kat1") String kat1,
            @Param("kat2") String kat2
    );

    @Query(value = """
            SELECT DISTINCT TRIM(s.stok_adi) AS ad
            FROM stoklar s
            WHERE s.stok_adi IS NOT NULL
              AND LENGTH(TRIM(s.stok_adi)) > 0
              AND LOWER(TRIM(s.marka)) IN (:markalar)
              AND TRIM(s.model_kodu) = TRIM(:model)
              AND (
                    LENGTH(TRIM(CAST(:kisa AS VARCHAR))) = 0
                    OR TRIM(s.kisa_ismi) = TRIM(CAST(:kisa AS VARCHAR))
              )
              AND TRIM(s.kategori_1) = TRIM(CAST(:kat1 AS VARCHAR))
              AND TRIM(s.kategori_2) = TRIM(CAST(:kat2 AS VARCHAR))
              AND (
                    LENGTH(TRIM(CAST(:altStok AS VARCHAR))) = 0
                    OR TRIM(COALESCE(s.alternatif_stok_adi, '')) = TRIM(CAST(:altStok AS VARCHAR))
              )
            ORDER BY TRIM(s.stok_adi)
            LIMIT 350
            """, nativeQuery = true)
    List<String> manuelDistinctStokAdi(
            @Param("markalar") List<String> markalar,
            @Param("model") String model,
            @Param("kisa") String kisa,
            @Param("kat1") String kat1,
            @Param("kat2") String kat2,
            @Param("altStok") String altStokAdi
    );
}

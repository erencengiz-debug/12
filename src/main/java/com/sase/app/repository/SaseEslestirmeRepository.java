package com.sase.app.repository;

import com.sase.app.entity.SaseEslestirme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SaseEslestirmeRepository extends JpaRepository<SaseEslestirme, UUID>,
        JpaSpecificationExecutor<SaseEslestirme> {

    List<SaseEslestirme> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /** VW liste: sıra = formül sırası (en eski kayıt 1) için created_at artan sıra */
    List<SaseEslestirme> findByUserIdOrderByCreatedAtAsc(UUID userId);

    Page<SaseEslestirme> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserId(UUID userId);

    long countByUserIdAndExecutedTrue(UUID userId);

    /**
     * Sadece bu kullanıcının bekleyen kayıtlarını çalıştırır (çoğu kiracılı senaryosu için güvenli).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE public.sase_eslestirme_vw
            SET executed = TRUE,
                executed_date = NOW(),
                updated_at = NOW()
            WHERE (executed = FALSE OR executed IS NULL)
              AND user_id = :userId
            """, nativeQuery = true)
    int calistirHepsiniKullaniciyaGore(@Param("userId") UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE public.sase_eslestirme_vw
            SET executed = TRUE,
                executed_date = NOW(),
                updated_at = NOW()
            WHERE id = :id
              AND user_id = :userId
            """, nativeQuery = true)
    int calistirByIdVeKullanici(@Param("id") UUID id, @Param("userId") UUID userId);

    /** Silme sırasında sahiplik doğrulaması */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SaseEslestirme e WHERE e.id = :id AND e.userId = :userId")
    int deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}

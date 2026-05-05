package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sase_eslestirme_vw")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaseEslestirme extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** auth.users.id — kaydı oluşturan kullanıcı */
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "baslik", nullable = false)
    private String baslik;

    /** Eşleştirmede kullanılacak stok ID listesi (PostgreSQL uuid[]) */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "stok_liste", columnDefinition = "uuid[]")
    private UUID[] stokListe;

    // ─── Şase Kodları ─────────────────────────────────────────────────────────
    @Column(name = "sase_kod_1") private String saseKod1;
    @Column(name = "sase_kod_2") private String saseKod2;
    @Column(name = "sase_kod_3") private String saseKod3;
    @Column(name = "sase_kod_4") private String saseKod4;
    @Column(name = "sase_kod_5") private String saseKod5;
    @Column(name = "sase_kod_6") private String saseKod6;
    @Column(name = "sase_kod_7") private String saseKod7;
    @Column(name = "sase_kod_8") private String saseKod8;
    @Column(name = "sase_kod_9") private String saseKod9;

    // ─── Araç Filtreleme Kriterleri ────────────────────────────────────────────
    @Column(name = "model")
    private String model;

    @Column(name = "uretim_tarihi_baslangic")
    private LocalDate uretimTarihiBaslangic;

    @Column(name = "uretim_tarihi_bitis")
    private LocalDate uretimTarihiBitis;

    @Column(name = "model_yili")
    private String modelYili;

    @Column(name = "satis_tipi")
    private String satisTipi;

    @Column(name = "motor_kodu")
    private String motorKodu;

    @Column(name = "sanziman_kodu")
    private String sanzimanKodu;

    @Column(name = "aks_tahrigi_tanimi")
    private String aksTahrigiTanimi;

    @Column(name = "eks_donanim")
    private String eksDonanim;

    // ─── Sonuç Bilgileri ───────────────────────────────────────────────────────
    @Column(name = "executed")
    private Boolean executed;

    @Column(name = "executed_date")
    private OffsetDateTime executedDate;

    @Column(name = "eslenik_sase_adedi")
    private Integer eslenikSaseAdedi;

    @Column(name = "degerli_aciklama_stok_kods", columnDefinition = "text")
    private String degerliAciklamaStokKods;
}

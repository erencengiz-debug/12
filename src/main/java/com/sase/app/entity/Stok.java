package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "stoklar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stok {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "stok_kodu")
    private String stokKodu;

    @Column(name = "orj_kodu")
    private String orjKodu;

    @Column(name = "model_kodu")
    private String modelKodu;

    @Column(name = "marka")
    private String marka;

    @Column(name = "stok_adi", nullable = false)
    private String stokAdi;

    @Column(name = "alternatif_stok_adi")
    private String alternatifStokAdi;

    @Column(name = "kisa_ismi")
    private String kisaIsmi;

    @Column(name = "alternatif_kisa_isim")
    private String alternatifKisaIsim;

    @Column(name = "kategori_1")
    private String kategori1;

    @Column(name = "kategori_2")
    private String kategori2;

    @Column(name = "fiyat_1", precision = 10, scale = 2)
    private BigDecimal fiyat1;

    @Column(name = "fiyat_7", precision = 10, scale = 2)
    private BigDecimal fiyat7;

    @Column(name = "fiyat_10", precision = 10, scale = 2)
    private BigDecimal fiyat10;

    @Column(name = "depo_merkez")
    private Integer depoMerkez;

    @Column(name = "depo_toplam")
    private Integer depoToplam;

    @Column(name = "stok_status")
    private Boolean stokStatus;

    @Column(name = "web_status")
    private Boolean webStatus;

    @Column(name = "bakim")
    private Boolean bakim;

    @Column(name = "rektifiye")
    private Boolean rektifiye;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // ─── İlişkiler ────────────────────────────────────────────────────────────

    @Builder.Default
    @OneToMany(mappedBy = "stok", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MuadilKod> muadilKodlar = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "stok", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<StokFotograf> fotograflar = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "stok", fetch = FetchType.LAZY)
    private List<StokAlis> alisList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "stok", fetch = FetchType.LAZY)
    private List<StokCikis> cikisList = new ArrayList<>();
}

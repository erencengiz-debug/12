package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stok_cikis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StokCikis extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stok_id", nullable = false)
    private Stok stok;

    @Column(name = "miktar", nullable = false)
    private Integer miktar;

    @Column(name = "birim_fiyat", precision = 10, scale = 2)
    private BigDecimal birimFiyat;

    @Column(name = "toplam_tutar", precision = 10, scale = 2)
    private BigDecimal toplamTutar;

    @Column(name = "musteri")
    private String musteri;

    @Column(name = "cikis_tarihi")
    private LocalDateTime cikisTarihi;

    @Column(name = "notlar", columnDefinition = "text")
    private String notlar;
}

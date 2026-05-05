package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "stok_alis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StokAlis {

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

    @Column(name = "tedarikci")
    private String tedarikci;

    @Column(name = "alis_tarihi")
    private OffsetDateTime alisTarihi;

    @Column(name = "notlar", columnDefinition = "text")
    private String notlar;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}

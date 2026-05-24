package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stok_alis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StokAlis extends CreatedAtEntity {

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
    private LocalDateTime alisTarihi;

    @Column(name = "notlar", columnDefinition = "text")
    private String notlar;
}

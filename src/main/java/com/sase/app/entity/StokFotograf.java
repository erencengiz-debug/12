package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "stok_fotograflari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StokFotograf extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stok_id", nullable = false)
    private Stok stok;

    @Column(name = "foto_url", nullable = false)
    private String fotoUrl;

    @Column(name = "sira")
    private Integer sira;
}

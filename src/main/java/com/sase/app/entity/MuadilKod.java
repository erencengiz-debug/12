package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "muadil_kodlar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuadilKod {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stok_id", nullable = false)
    private Stok stok;

    @Column(name = "muadil_kod", nullable = false)
    private String muadilKod;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}

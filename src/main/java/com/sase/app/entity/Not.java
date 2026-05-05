package com.sase.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notlar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Not extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** auth.users.id — Supabase tarafından yönetilen kullanıcı kimliği */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "baslik", nullable = false)
    private String baslik;

    @Column(name = "aciklama", columnDefinition = "text")
    private String aciklama;

    @Column(name = "tarih", nullable = false)
    private OffsetDateTime tarih;
}

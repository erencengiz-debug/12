package com.sase.app.dto.not;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotDto(
        UUID id,
        String baslik,
        String aciklama,
        OffsetDateTime tarih,
        OffsetDateTime createdAt
) {}

package com.sase.app.dto.sase;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SaseEslestirmeListDto(
        UUID id,
        String baslik,
        String saseKod1,
        String saseKod2,
        String saseKod3,
        String model,
        Boolean executed,
        OffsetDateTime executedDate,
        Integer eslenikSaseAdedi,
        OffsetDateTime createdAt
) {}

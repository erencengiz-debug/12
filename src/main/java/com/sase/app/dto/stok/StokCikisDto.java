package com.sase.app.dto.stok;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StokCikisDto(
        UUID id,
        Integer miktar,
        BigDecimal birimFiyat,
        BigDecimal toplamTutar,
        String musteri,
        OffsetDateTime cikisTarihi,
        String notlar
) {}

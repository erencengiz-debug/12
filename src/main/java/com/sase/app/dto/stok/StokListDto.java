package com.sase.app.dto.stok;

import java.math.BigDecimal;
import java.util.UUID;

public record StokListDto(
        UUID id,
        String stokKodu,
        String stokAdi,
        String kisaIsmi,
        String marka,
        String kategori1,
        String kategori2,
        BigDecimal fiyat1,
        Integer depoMerkez,
        Boolean stokStatus
) {}

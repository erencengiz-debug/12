package com.sase.app.dto.stok;

import java.math.BigDecimal;
import java.util.UUID;

public record StokDetailDto(
        UUID id,
        String stokKodu,
        String orjKodu,
        String modelKodu,
        String marka,
        String stokAdi,
        String alternatifStokAdi,
        String kisaIsmi,
        String alternatifKisaIsim,
        String kategori1,
        String kategori2,
        BigDecimal fiyat1,
        BigDecimal fiyat7,
        BigDecimal fiyat10,
        Integer depoMerkez,
        Integer depoToplam,
        Boolean stokStatus,
        Boolean webStatus,
        Boolean bakim,
        Boolean rektifiye
) {}

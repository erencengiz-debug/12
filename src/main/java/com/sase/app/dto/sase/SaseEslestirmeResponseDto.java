package com.sase.app.dto.sase;

import com.sase.app.dto.stok.StokOzetDto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SaseEslestirmeResponseDto(
        UUID id,
        UUID userId,
        String kullaniciAdi,
        String adSoyad,
        String baslik,
        UUID[] stokListe,
        List<StokOzetDto> stokDetaylari,
        String saseKod1,
        String saseKod2,
        String saseKod3,
        String saseKod4,
        String saseKod5,
        String saseKod6,
        String saseKod7,
        String saseKod8,
        String saseKod9,
        String model,
        LocalDate uretimTarihiBaslangic,
        LocalDate uretimTarihiBitis,
        String modelYili,
        String satisTipi,
        String motorKodu,
        String sanzimanKodu,
        String aksTahrigiTanimi,
        String eksDonanim,
        Boolean executed,
        OffsetDateTime executedDate,
        Integer eslenikSaseAdedi,
        String degerliAciklamaStokKods,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

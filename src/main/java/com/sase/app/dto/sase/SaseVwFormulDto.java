package com.sase.app.dto.sase;

import java.time.LocalDate;

/**
 * Liste + API çıktısı: {@code sase_vw_formuls} kaydı (JWKS dışında düz JSON).
 */
public record SaseVwFormulDto(
        Integer id,
        String ekleyenKullanici,
        String baslik,
        String stokListe,
        String saseKod1,
        String saseKod2,
        String saseKod3,
        String saseKod4,
        String saseKod5,
        String saseKod6,
        String saseKod7,
        String saseKod8,
        String saseKod9,
        String saseNo,
        String model,
        LocalDate uretimTarihiBas,
        LocalDate uretimTarihiBit,
        String modelYili,
        String satisTipi,
        String motorKodu,
        String sanzimanKodu,
        String aksTahrigiTanimi,
        String donanim,
        Boolean executed,
        LocalDate executeDate,
        Integer eslenikSaseAdedi,
        String degerliAciklamaStokKods
) {}

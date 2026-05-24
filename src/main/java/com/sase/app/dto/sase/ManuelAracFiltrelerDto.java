package com.sase.app.dto.sase;

import java.util.List;

/** /sase manuel araç seçim sayfası — stok kolonlarına göre cascade dropdown seçenekleri */
public record ManuelAracFiltrelerDto(
        List<String> modeller,
        List<String> altModeller,
        List<String> yillar,
        List<String> kategori1Liste,
        List<String> kategori2Liste,
        List<String> aciklama1Liste,
        List<String> aciklama2Liste
) {}

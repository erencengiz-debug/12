package com.sase.app.service;

import com.sase.app.dto.sase.ManuelAracFiltrelerDto;
import com.sase.app.repository.StokRepository;
import com.sase.app.sasehub.ManuelAracMarka;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManuelAracOptionService {

    private final StokRepository stokRepository;

    public ManuelAracFiltrelerDto filtreler(
            String markaSlug,
            String model,
            String altModel,
            String kat1,
            String kat2,
            String aciklama1
    ) {
        List<String> markalar = ManuelAracMarka.markaVariantsForStok(markaSlug);
        if (markalar.isEmpty()) {
            throw new IllegalArgumentException("Marka seçiniz veya slug geçersiz.");
        }

        List<String> modeller = stokRepository.manuelDistinctModelKodu(markalar);
        String m = nz(model);
        List<String> altModeller = StringUtils.hasText(m)
                ? stokRepository.manuelDistinctKisaIsmi(markalar, m)
                : List.of();

        String kisa = nz(altModel);
        List<String> kat1Liste = StringUtils.hasText(m)
                ? stokRepository.manuelDistinctKategori1(markalar, m, kisa)
                : List.of();

        String k1 = nz(kat1);
        List<String> kat2Liste = StringUtils.hasText(m) && StringUtils.hasText(k1)
                ? stokRepository.manuelDistinctKategori2(markalar, m, kisa, k1)
                : List.of();

        String k2 = nz(kat2);
        List<String> ack1Liste = StringUtils.hasText(m) && StringUtils.hasText(k1) && StringUtils.hasText(k2)
                ? stokRepository.manuelDistinctAlternatifStokAdi(markalar, m, kisa, k1, k2)
                : List.of();

        String a1 = nz(aciklama1);
        List<String> ack2Liste = StringUtils.hasText(m) && StringUtils.hasText(k1) && StringUtils.hasText(k2) && StringUtils.hasText(a1)
                ? stokRepository.manuelDistinctStokAdi(markalar, m, kisa, k1, k2, a1)
                : List.of();

        return new ManuelAracFiltrelerDto(
                safeList(modeller),
                safeList(altModeller),
                yillar(),
                safeList(kat1Liste),
                safeList(kat2Liste),
                safeList(ack1Liste),
                safeList(ack2Liste)
        );
    }

    private static List<String> safeList(List<String> in) {
        return in == null ? List.of() : List.copyOf(in);
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }

    private static List<String> yillar() {
        int y = Year.now().getValue();
        List<String> o = new ArrayList<>();
        for (int i = y; i >= 1980; i--) {
            o.add(Integer.toString(i));
        }
        return o;
    }
}

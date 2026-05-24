package com.sase.app.dto.sase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST form gövdesi.
 * {@code stokListe} koleksiyon olarak tanımlandı — JSON dizisinin Jackson ile güvenilir ayrışması için.
 */
public record SaseEslestirmeRequestDto(
        List<UUID> stokListe,
        String degerliAciklamaStokKods,

        @NotBlank(message = "Formül başlığı boş olamaz")
        @Size(max = 500)
        String baslik,

        @Size(max = 2000) String saseKod1,
        @Size(max = 2000) String saseKod2,
        @Size(max = 2000) String saseKod3,
        @Size(max = 2000) String saseKod4,
        @Size(max = 2000) String saseKod5,
        @Size(max = 2000) String saseKod6,
        @Size(max = 2000) String saseKod7,
        @Size(max = 2000) String saseKod8,
        @Size(max = 2000) String saseKod9,

        @Size(max = 500) String model,
        @Size(max = 200) String modelYili,
        LocalDate uretimTarihiBaslangic,
        LocalDate uretimTarihiBitis,
        @Size(max = 4000) String satisTipi,
        @Size(max = 8000) String motorKodu,
        @Size(max = 800) String sanzimanKodu,
        @Size(max = 800) String aksTahrigiTanimi,
        @Size(max = 800) String eksDonanim
) {
    public UUID[] stokListeNormalized() {
        if (stokListe == null || stokListe.isEmpty()) {
            return new UUID[0];
        }
        List<UUID> filtered = new ArrayList<>();
        for (UUID id : stokListe) {
            if (id != null) {
                filtered.add(id);
            }
        }
        return filtered.toArray(UUID[]::new);
    }
}

package com.sase.app.dto.sase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SaseEslestirmeForm(

        @NotBlank(message = "Başlık boş olamaz")
        @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
        String baslik,

        @Size(max = 50, message = "Şase kodu en fazla 50 karakter olabilir")
        String saseKod1,
        @Size(max = 50) String saseKod2,
        @Size(max = 50) String saseKod3,
        @Size(max = 50) String saseKod4,
        @Size(max = 50) String saseKod5,
        @Size(max = 50) String saseKod6,
        @Size(max = 50) String saseKod7,
        @Size(max = 50) String saseKod8,
        @Size(max = 50) String saseKod9,

        String model,
        String modelYili,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate uretimTarihiBaslangic,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate uretimTarihiBitis,

        String motorKodu,
        String sanzimanKodu,
        String satisTipi,
        String aksTahrigiTanimi
) {}

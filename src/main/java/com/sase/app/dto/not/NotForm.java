package com.sase.app.dto.not;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record NotForm(

        @NotBlank(message = "Başlık boş olamaz")
        @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
        String baslik,

        @Size(max = 5000, message = "Açıklama en fazla 5000 karakter olabilir")
        String aciklama,

        @NotNull(message = "Tarih boş olamaz")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate tarih
) {}

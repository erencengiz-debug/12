package com.sase.app.controller.web;

import com.sase.app.entity.SaseEslestirme;
import com.sase.app.mapper.SaseEslestirmeMapper;
import com.sase.app.service.SaseEslestirmeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/sase")
@RequiredArgsConstructor
public class SaseEslestirmeController {

    private final SaseEslestirmeService saseEslestirmeService;
    private final SaseEslestirmeMapper saseEslestirmeMapper;

    @GetMapping
    public String list(@AuthenticationPrincipal Jwt jwt, Model model) {
        UUID userId = UUID.fromString(jwt.getSubject());
        model.addAttribute("eslestirmeler",
                saseEslestirmeMapper.toListDtos(saseEslestirmeService.kullaniciyaAit(userId)));
        model.addAttribute("activePage", "sase");
        return "sase/list";
    }

    @GetMapping("/yeni")
    public String form(Model model) {
        model.addAttribute("activePage", "sase");
        return "sase/form";
    }

    @PostMapping
    public String save(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String baslik,
            @RequestParam(required = false) String saseKod1,
            @RequestParam(required = false) String saseKod2,
            @RequestParam(required = false) String saseKod3,
            @RequestParam(required = false) String saseKod4,
            @RequestParam(required = false) String saseKod5,
            @RequestParam(required = false) String saseKod6,
            @RequestParam(required = false) String saseKod7,
            @RequestParam(required = false) String saseKod8,
            @RequestParam(required = false) String saseKod9,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String modelYili,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uretimTarihiBaslangic,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uretimTarihiBitis,
            @RequestParam(required = false) String motorKodu,
            @RequestParam(required = false) String sanzimanKodu,
            @RequestParam(required = false) String satisTipi,
            @RequestParam(required = false) String aksTahrigiTanimi,
            RedirectAttributes ra
    ) {
        SaseEslestirme eslestirme = SaseEslestirme.builder()
                .userId(UUID.fromString(jwt.getSubject()))
                .baslik(baslik)
                .saseKod1(blankToNull(saseKod1))
                .saseKod2(blankToNull(saseKod2))
                .saseKod3(blankToNull(saseKod3))
                .saseKod4(blankToNull(saseKod4))
                .saseKod5(blankToNull(saseKod5))
                .saseKod6(blankToNull(saseKod6))
                .saseKod7(blankToNull(saseKod7))
                .saseKod8(blankToNull(saseKod8))
                .saseKod9(blankToNull(saseKod9))
                .model(blankToNull(model))
                .modelYili(blankToNull(modelYili))
                .uretimTarihiBaslangic(uretimTarihiBaslangic)
                .uretimTarihiBitis(uretimTarihiBitis)
                .motorKodu(blankToNull(motorKodu))
                .sanzimanKodu(blankToNull(sanzimanKodu))
                .satisTipi(blankToNull(satisTipi))
                .aksTahrigiTanimi(blankToNull(aksTahrigiTanimi))
                .executed(false)
                .build();

        SaseEslestirme saved = saseEslestirmeService.kaydet(eslestirme);
        ra.addFlashAttribute("successMsg", "Eşleştirme kaydedildi.");
        return "redirect:/sase/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("e", saseEslestirmeService.idIleGetir(id));
            model.addAttribute("activePage", "sase");
        } catch (EntityNotFoundException ex) {
            ra.addFlashAttribute("errorMsg", "Kayıt bulunamadı.");
            return "redirect:/sase";
        }
        return "sase/detail";
    }

    @PostMapping("/{id}/calistir")
    public String calistir(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            saseEslestirmeService.calistir(id);
            ra.addFlashAttribute("successMsg", "Eşleştirme çalıştırıldı.");
        } catch (EntityNotFoundException ex) {
            ra.addFlashAttribute("errorMsg", "Kayıt bulunamadı.");
        }
        return "redirect:/sase/" + id;
    }

    @PostMapping("/{id}/sil")
    public String sil(@PathVariable UUID id, RedirectAttributes ra) {
        saseEslestirmeService.sil(id);
        ra.addFlashAttribute("successMsg", "Kayıt silindi.");
        return "redirect:/sase";
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

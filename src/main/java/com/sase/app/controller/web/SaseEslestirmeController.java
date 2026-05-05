package com.sase.app.controller.web;

import com.sase.app.dto.sase.SaseEslestirmeForm;
import com.sase.app.entity.SaseEslestirme;
import com.sase.app.mapper.SaseEslestirmeMapper;
import com.sase.app.service.SaseEslestirmeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            @Valid SaseEslestirmeForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("activePage", "sase");
            return "sase/form";
        }

        SaseEslestirme eslestirme = SaseEslestirme.builder()
                .userId(UUID.fromString(jwt.getSubject()))
                .baslik(form.baslik())
                .saseKod1(blankToNull(form.saseKod1()))
                .saseKod2(blankToNull(form.saseKod2()))
                .saseKod3(blankToNull(form.saseKod3()))
                .saseKod4(blankToNull(form.saseKod4()))
                .saseKod5(blankToNull(form.saseKod5()))
                .saseKod6(blankToNull(form.saseKod6()))
                .saseKod7(blankToNull(form.saseKod7()))
                .saseKod8(blankToNull(form.saseKod8()))
                .saseKod9(blankToNull(form.saseKod9()))
                .model(blankToNull(form.model()))
                .modelYili(blankToNull(form.modelYili()))
                .uretimTarihiBaslangic(form.uretimTarihiBaslangic())
                .uretimTarihiBitis(form.uretimTarihiBitis())
                .motorKodu(blankToNull(form.motorKodu()))
                .sanzimanKodu(blankToNull(form.sanzimanKodu()))
                .satisTipi(blankToNull(form.satisTipi()))
                .aksTahrigiTanimi(blankToNull(form.aksTahrigiTanimi()))
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

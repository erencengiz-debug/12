package com.sase.app.controller.web;

import com.sase.app.dto.not.NotForm;
import com.sase.app.entity.Not;
import com.sase.app.mapper.NotMapper;
import com.sase.app.service.NotService;
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
@RequestMapping("/not")
@RequiredArgsConstructor
public class NotController {

    private final NotService notService;
    private final NotMapper notMapper;

    @GetMapping
    public String list(@AuthenticationPrincipal Jwt jwt, Model model) {
        UUID userId = UUID.fromString(jwt.getSubject());
        model.addAttribute("notlar", notMapper.toDtos(notService.kullaniciyaAit(userId)));
        model.addAttribute("activePage", "not");
        return "not/list";
    }

    @GetMapping("/yeni")
    public String form(Model model) {
        model.addAttribute("activePage", "not");
        return "not/form";
    }

    @PostMapping
    public String save(
            @AuthenticationPrincipal Jwt jwt,
            @Valid NotForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes ra
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            model.addAttribute("activePage", "not");
            return "not/form";
        }

        Not not = Not.builder()
                .userId(UUID.fromString(jwt.getSubject()))
                .baslik(form.baslik())
                .aciklama(form.aciklama() != null && !form.aciklama().isBlank() ? form.aciklama() : null)
                .tarih(form.tarih().atStartOfDay())
                .build();

        notService.kaydet(not);
        ra.addFlashAttribute("successMsg", "Not kaydedildi.");
        return "redirect:/not";
    }

    @PostMapping("/{id}/sil")
    public String sil(@PathVariable UUID id, RedirectAttributes ra) {
        try {
            notService.sil(id);
            ra.addFlashAttribute("successMsg", "Not silindi.");
        } catch (EntityNotFoundException ex) {
            ra.addFlashAttribute("errorMsg", "Not bulunamadı.");
        }
        return "redirect:/not";
    }
}

package com.sase.app.controller.web;

import com.sase.app.entity.Stok;
import com.sase.app.service.StokService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/stok")
@RequiredArgsConstructor
public class StokController {

    private final StokService stokService;

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("stoklar", q != null && !q.isBlank()
                ? stokService.ara(q)
                : stokService.hepsiniGetir());
        model.addAttribute("q", q);
        model.addAttribute("activePage", "stok");
        return "stok/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        try {
            Stok stok = stokService.idIleGetir(id);
            model.addAttribute("stok", stok);
            model.addAttribute("aktivePage", "stok");
        } catch (EntityNotFoundException e) {
            return "redirect:/stok?errorMsg=Stok+bulunamadı";
        }
        return "stok/detail";
    }
}

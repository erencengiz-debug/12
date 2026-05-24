package com.sase.app.controller.web;

import com.sase.app.mapper.StokMapper;
import com.sase.app.service.StokService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/stok")
@RequiredArgsConstructor
public class StokController {

    private final StokService stokService;
    private final StokMapper stokMapper;

    /**
     * Stok SPA (tek sayfa form + grid). Layout ile üst navbar sabit kalır.
     */
    @GetMapping
    public String stokForm(Model model) {
        model.addAttribute("activePage", "stok");
        return "stok/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        model.addAttribute("stok", stokMapper.toDetailDto(stokService.detayGetir(id)));
        model.addAttribute("activePage", "stok");
        return "stok/detail";
    }
}

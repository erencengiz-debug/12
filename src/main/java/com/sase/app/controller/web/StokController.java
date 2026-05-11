package com.sase.app.controller.web;

import com.sase.app.mapper.StokMapper;
import com.sase.app.service.StokService;
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
    private final StokMapper stokMapper;

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        var stokPage = stokService.listele(q, page);
        model.addAttribute("stoklar", stokMapper.toListDtos(stokPage.getContent()));
        model.addAttribute("stokPage", stokPage);
        model.addAttribute("q", q);
        model.addAttribute("activePage", "stok");
        return "stok/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable UUID id, Model model) {
        model.addAttribute("stok", stokMapper.toDetailDto(stokService.detayGetir(id)));
        model.addAttribute("activePage", "stok");
        return "stok/detail";
    }
}

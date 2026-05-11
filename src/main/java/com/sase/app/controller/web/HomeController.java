package com.sase.app.controller.web;

import com.sase.app.repository.NotRepository;
import com.sase.app.repository.SaseEslestirmeRepository;
import com.sase.app.repository.StokRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final StokRepository stokRepository;
    private final SaseEslestirmeRepository saseEslestirmeRepository;
    private final NotRepository notRepository;

    @GetMapping("/")
    public String home(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt != null) {
            model.addAttribute("email", jwt.getClaimAsString("email"));
            model.addAttribute("userId", jwt.getSubject());

            UUID userId = UUID.fromString(jwt.getSubject());

            // 4 COUNT sorgusu paralel çalışır — toplam ~400ms (sıralı olsaydı ~1.2s)
            var cfStok = CompletableFuture
                    .supplyAsync(stokRepository::count)
                    .exceptionally(e -> 0L);
            var cfSase = CompletableFuture
                    .supplyAsync(() -> saseEslestirmeRepository.countByUserId(userId))
                    .exceptionally(e -> 0L);
            var cfNot = CompletableFuture
                    .supplyAsync(() -> notRepository.countByUserId(userId))
                    .exceptionally(e -> 0L);
            var cfTam = CompletableFuture
                    .supplyAsync(() -> saseEslestirmeRepository.countByUserIdAndExecutedTrue(userId))
                    .exceptionally(e -> 0L);

            CompletableFuture.allOf(cfStok, cfSase, cfNot, cfTam).join();

            model.addAttribute("stokSayisi",       cfStok.join());
            model.addAttribute("saseSayisi",       cfSase.join());
            model.addAttribute("notSayisi",        cfNot.join());
            model.addAttribute("tamamlananSayisi", cfTam.join());
        }
        model.addAttribute("activePage", "dashboard");
        return "index";
    }
}

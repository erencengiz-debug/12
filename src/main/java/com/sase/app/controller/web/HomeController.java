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
            model.addAttribute("stokSayisi", stokRepository.count());
            model.addAttribute("saseSayisi", saseEslestirmeRepository.countByUserId(userId));
            model.addAttribute("notSayisi", notRepository.countByUserId(userId));
            model.addAttribute("tamamlananSayisi",
                    saseEslestirmeRepository.countByUserIdAndExecutedTrue(userId));
        }
        model.addAttribute("activePage", "dashboard");
        return "index";
    }
}

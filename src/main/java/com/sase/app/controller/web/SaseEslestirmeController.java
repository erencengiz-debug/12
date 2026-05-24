package com.sase.app.controller.web;

import com.sase.app.entity.SaseEslestirme;
import com.sase.app.service.SaseEslestirmeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/sase")
@RequiredArgsConstructor
public class SaseEslestirmeController {

    private static final Set<String> FORMUL_GRUP_KODLARI =
            Set.of("vw", "psa", "opel", "fiat");

    private final SaseEslestirmeService saseEslestirmeService;

    @GetMapping
    public String list(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt == null) {
            return "redirect:/login";
        }
        model.addAttribute("activePage", "sase");
        return "sase/list";
    }

    /**
     * Marka bazlı şase-formül liste sayfaları; URL <code>/sase/formul/&lt;kod&gt;</code> olarak tanımlandı ki
     * <code>/sase/{id}</code> (UUID) ile çakılmasın.
     */
    @GetMapping("/formul/{grup}")
    public String formulListe(@AuthenticationPrincipal Jwt jwt, @PathVariable String grup, Model model) {
        String normalized = grup == null ? "" : grup.trim().toLowerCase(Locale.ROOT);
        if (!FORMUL_GRUP_KODLARI.contains(normalized)) {
            return "redirect:/sase";
        }
        if ("vw".equals(normalized)) {
            return "redirect:/sase/vw/formuls";
        }
        return "redirect:/sase/" + normalized;
    }

    /** VW üst giriş — alt sayfalar: {@code /sase/vw/formuls} vb. */
    @GetMapping("/vw")
    public String saseVwUstSayfa(Model model, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUserJwtSub", jwt.getSubject());
        UUID uid = UUID.fromString(jwt.getSubject());
        model.addAttribute("activePage", "sase-vw");
        model.addAttribute("vwRows", saseEslestirmeService.vwFormulListeSatirlari(uid));
        model.addAttribute("ekleyenOptions", saseEslestirmeService.vwFormulListeEkleyenSecenekleri(uid));
        return "sase/vw-hub";
    }

    /** VW formül & liste (eski {@code /sase/vw}) */
    @GetMapping("/vw/formuls")
    public String saseVwFormuls(Model model, @AuthenticationPrincipal Jwt jwt) {
        return formulEslestirmeSayfa(model, jwt, "VW");
    }

    @GetMapping("/psa")
    public String sasePsa(Model model, @AuthenticationPrincipal Jwt jwt) {
        return formulEslestirmeSayfa(model, jwt, "PSA");
    }

    @GetMapping("/opel")
    public String saseOpel(Model model, @AuthenticationPrincipal Jwt jwt) {
        return formulEslestirmeSayfa(model, jwt, "OPEL");
    }

    @GetMapping("/fiat")
    public String saseFiat(Model model, @AuthenticationPrincipal Jwt jwt) {
        return formulEslestirmeSayfa(model, jwt, "FIAT");
    }

    /** Tam ekran form + tablo; veri AJAX ile {@code /api/sase}'den çekilir. */
    private String formulEslestirmeSayfa(Model model, Jwt jwt, String marka) {
        if (jwt != null) {
            model.addAttribute("currentUserJwtSub", jwt.getSubject());
        }
        model.addAttribute("marka", marka);
        model.addAttribute("activePage", "sase-formul-" + marka.toLowerCase(Locale.ROOT));
        return "sase/sase-eslestirme";
    }

    /** Eski adres; formül ekranına yönlendirir ({@code /sase/vw/formuls}). */
    @GetMapping("/yeni")
    public String yeniEskiAdresRedirect() {
        return "redirect:/sase/vw/formuls";
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
    public String calistir(@AuthenticationPrincipal Jwt jwt,
                           @PathVariable UUID id,
                           RedirectAttributes ra) {
        try {
            saseEslestirmeService.calistir(id, UUID.fromString(jwt.getSubject()));
            ra.addFlashAttribute("successMsg", "Eşleştirme çalıştırıldı.");
        } catch (EntityNotFoundException ex) {
            ra.addFlashAttribute("errorMsg", "Kayıt bulunamadı.");
        }
        return "redirect:/sase/" + id;
    }

    @PostMapping("/{id}/sil")
    public String sil(@AuthenticationPrincipal Jwt jwt,
                      @PathVariable UUID id,
                      RedirectAttributes ra) {
        try {
            saseEslestirmeService.silGuvenli(id, UUID.fromString(jwt.getSubject()));
            ra.addFlashAttribute("successMsg", "Kayıt silindi.");
        } catch (EntityNotFoundException ex) {
            ra.addFlashAttribute("errorMsg", "Kayıt silinemedi veya size ait değil.");
        }
        return "redirect:/sase";
    }
}

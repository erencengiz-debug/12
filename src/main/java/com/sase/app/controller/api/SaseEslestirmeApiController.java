package com.sase.app.controller.api;

import com.sase.app.dto.common.ApiResponse;
import com.sase.app.dto.sase.ManuelAracFiltrelerDto;
import com.sase.app.dto.sase.SaseEslestirmeListDto;
import com.sase.app.dto.sase.SaseEslestirmeRequestDto;
import com.sase.app.dto.sase.SaseEslestirmeResponseDto;
import com.sase.app.dto.sase.SaseListeResponseDto;
import com.sase.app.dto.stok.StokOzetDto;
import com.sase.app.service.ManuelAracOptionService;
import com.sase.app.service.SaseEslestirmeService;
import com.sase.app.service.StokService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sase")
@RequiredArgsConstructor
public class SaseEslestirmeApiController {

    private final SaseEslestirmeService saseEslestirmeService;
    private final StokService stokService;
    private final ManuelAracOptionService manuelAracOptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<SaseListeResponseDto>> liste(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam Map<String, String> query
    ) {
        UUID uid = kullaniciId(jwt);
        Map<String, String> filters = new LinkedHashMap<>(query);
        filters.remove("page");
        filters.remove("size");
        SaseListeResponseDto liste = saseEslestirmeService.apiListe(uid, page, size, filters);
        return ResponseEntity.ok(ApiResponse.ok(liste, "İşlem başarılı"));
    }

    /**
     * /sase hub — marka seçimine bağlı cascade filtre listeleri (stok kolonları).
     * {@code GET /api/sase/manuel-arac/filtreler}
     */
    @GetMapping("/manuel-arac/filtreler")
    public ResponseEntity<ApiResponse<ManuelAracFiltrelerDto>> manuelAracFiltreler(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("markaSlug") String markaSlug,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String altModel,
            @RequestParam(required = false) String kat1,
            @RequestParam(required = false) String kat2,
            @RequestParam(required = false) String aciklama1
    ) {
        kullaniciId(jwt);
        ManuelAracFiltrelerDto dto = manuelAracOptionService.filtreler(
                markaSlug, model, altModel, kat1, kat2, aciklama1
        );
        return ResponseEntity.ok(ApiResponse.ok(dto, "İşlem başarılı"));
    }

    /**
     * Kullanıcının kendi şase formüllerinde metin arar (şase alanları, model, başlık).
     */
    @GetMapping("/arama/kayit-metni")
    public ResponseEntity<ApiResponse<List<SaseEslestirmeListDto>>> aramaKayitMetni(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("q") String q
    ) {
        UUID uid = kullaniciId(jwt);
        List<SaseEslestirmeListDto> bulunan = saseEslestirmeService.kullaniciKayitlarindaMetinAra(uid, q);
        return ResponseEntity.ok(ApiResponse.ok(bulunan, bulunan.size() + " eşleşme"));
    }

    @GetMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<ApiResponse<SaseEslestirmeResponseDto>> tekil(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID uid = kullaniciId(jwt);
        SaseEslestirmeResponseDto dto = saseEslestirmeService.apiTekil(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(dto, "İşlem başarılı"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SaseEslestirmeResponseDto>> olustur(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SaseEslestirmeRequestDto dto
    ) {
        UUID uid = kullaniciId(jwt);
        SaseEslestirmeResponseDto kayit = saseEslestirmeService.apiOlustur(dto, uid);
        return ResponseEntity.ok(ApiResponse.ok(kayit, "Kayıt oluşturuldu"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SaseEslestirmeResponseDto>> guncelle(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody SaseEslestirmeRequestDto dto
    ) {
        UUID uid = kullaniciId(jwt);
        SaseEslestirmeResponseDto kayit = saseEslestirmeService.apiGuncelle(uid, id, dto);
        return ResponseEntity.ok(ApiResponse.ok(kayit, "Güncelleme yapıldı"));
    }

    @DeleteMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}")
    public ResponseEntity<ApiResponse<Void>> sil(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID uid = kullaniciId(jwt);
        saseEslestirmeService.apiSil(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Kayıt silindi"));
    }

    @PostMapping("/calistir-hepsini")
    public ResponseEntity<ApiResponse<Integer>> calistirHepsini(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = kullaniciId(jwt);
        int n = saseEslestirmeService.apiCalistirHepsini(uid);
        return ResponseEntity.ok(ApiResponse.ok(n, n + " kayıt çalıştırıldı"));
    }

    @PostMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/calistir")
    public ResponseEntity<ApiResponse<Void>> calistirTek(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID uid = kullaniciId(jwt);
        saseEslestirmeService.apiCalistirTek(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Kayıt çalıştırıldı"));
    }

    @PostMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/alan-temizle")
    public ResponseEntity<ApiResponse<Void>> vwAlanTemizle(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID uid = kullaniciId(jwt);
        saseEslestirmeService.apiVwListeAlanTemizle(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Alanlar sıfırlandı"));
    }

    /**
     * Kayıtlı formülün kopyasını oluşturur ({@code executed=false}, yeni kimlik).
     */
    @PostMapping("/{id:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}}/kopyala")
    public ResponseEntity<ApiResponse<SaseEslestirmeResponseDto>> formulKopyala(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID uid = kullaniciId(jwt);
        SaseEslestirmeResponseDto kayit = saseEslestirmeService.apiFormulKopyala(uid, id);
        return ResponseEntity.ok(ApiResponse.ok(kayit, "Formül kopyalandı"));
    }

    @GetMapping("/stok-ara")
    public ResponseEntity<ApiResponse<List<StokOzetDto>>> stokAra(
            @RequestParam(value = "q", defaultValue = "") String q,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        List<StokOzetDto> liste = stokService.stokOzetiAra(q, limit);
        return ResponseEntity.ok(ApiResponse.ok(liste, "İşlem başarılı"));
    }

    private static UUID kullaniciId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalArgumentException("Oturum gerekli.");
        }
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new IllegalArgumentException("Oturum bilgisi eksik (subject).");
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Geçersiz kullanıcı kimliği (JWT subject).");
        }
    }
}

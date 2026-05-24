package com.sase.app.service;

import com.sase.app.dto.sase.SaseEslestirmeListDto;
import com.sase.app.dto.sase.SaseEslestirmeRequestDto;
import com.sase.app.dto.sase.SaseEslestirmeResponseDto;
import com.sase.app.dto.sase.SaseListeResponseDto;
import com.sase.app.dto.sase.SaseVwListRowDto;
import com.sase.app.dto.stok.StokOzetDto;
import com.sase.app.entity.Profile;
import com.sase.app.entity.SaseEslestirme;
import com.sase.app.entity.Stok;
import com.sase.app.mapper.SaseEslestirmeMapper;
import com.sase.app.repository.ProfileRepository;
import com.sase.app.repository.SaseEslestirmeRepository;
import com.sase.app.repository.StokRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaseEslestirmeService {

    static final int MAX_PAGE_SIZE = 100;

    /** Sütun filtresinde izin verilen alan adları (root.get güvenli). */
    private static final Set<String> LIST_FILTER_FIELDS = Set.of(
            "baslik",
            "saseKod1", "saseKod2", "saseKod3", "saseKod4",
            "saseKod5", "saseKod6", "saseKod7", "saseKod8", "saseKod9",
            "model", "modelYili", "satisTipi", "motorKodu",
            "sanzimanKodu", "aksTahrigiTanimi", "eksDonanim",
            "degerliAciklamaStokKods"
    );

    private final SaseEslestirmeRepository saseEslestirmeRepository;
    private final ProfileRepository profileRepository;
    private final StokRepository stokRepository;
    private final SaseEslestirmeMapper saseEslestirmeMapper;

    private Map<UUID, Profile> profileBatchCache = Map.of();
    private SaseEslestirmeService self;

    @Lazy
    @Autowired
    void setSelf(SaseEslestirmeService self) {
        this.self = self;
    }

    public List<SaseEslestirme> kullaniciyaAit(UUID userId) {
        return saseEslestirmeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /** Kullanıcının şase formüllerinde serbest metin arar (şase kodları / model / başlık). */
    public List<SaseEslestirmeListDto> kullaniciKayitlarindaMetinAra(UUID userId, String metin) {
        if (!StringUtils.hasText(metin)) {
            return List.of();
        }
        String q = metin.trim().toLowerCase(Locale.ROOT);
        return kullaniciyaAit(userId).stream()
                .filter(e -> saseKaydindaBul(e, q))
                .map(saseEslestirmeMapper::toListDto)
                .toList();
    }

    private static boolean saseKaydindaBul(SaseEslestirme e, String qq) {
        if (icc(e.getBaslik(), qq)) return true;
        if (icc(e.getModel(), qq)) return true;
        if (icc(e.getModelYili(), qq)) return true;
        if (icc(e.getSatisTipi(), qq)) return true;
        if (icc(e.getMotorKodu(), qq)) return true;
        if (icc(e.getSanzimanKodu(), qq)) return true;
        return saseKodlarindaBul(e, qq);
    }

    private static boolean saseKodlarindaBul(SaseEslestirme e, String qq) {
        return icc(e.getSaseKod1(), qq)
                || icc(e.getSaseKod2(), qq)
                || icc(e.getSaseKod3(), qq)
                || icc(e.getSaseKod4(), qq)
                || icc(e.getSaseKod5(), qq)
                || icc(e.getSaseKod6(), qq)
                || icc(e.getSaseKod7(), qq)
                || icc(e.getSaseKod8(), qq)
                || icc(e.getSaseKod9(), qq);
    }

    private static boolean icc(String field, String q) {
        if (field == null || field.isBlank()) return false;
        return field.toLowerCase(Locale.ROOT).contains(q);
    }

    public SaseListeResponseDto apiListe(UUID userId, int pageZero, int requestedSize, Map<String, String> filters) {
        int size = sanitizePageSize(requestedSize);
        Pageable pageable = PageRequest.of(pageZero, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<SaseEslestirme> spec = buildListSpec(userId, filters != null ? filters : Map.of());
        Page<SaseEslestirme> pg = saseEslestirmeRepository.findAll(spec, pageable);
        profileBatchCache = preloadProfiles(pg.getContent());
        try {
            List<SaseEslestirmeResponseDto> dtoList = pg.getContent().stream()
                    .map(this::toResponseFromCache)
                    .toList();
            return new SaseListeResponseDto(
                    dtoList,
                    pg.getTotalElements(),
                    pageZero,
                    size,
                    pg.getTotalPages()
            );
        } finally {
            profileBatchCache = Map.of();
        }
    }

    public SaseEslestirmeResponseDto apiTekil(UUID userId, UUID id) {
        SaseEslestirme e = idIleGetir(id);
        ownership(e, userId);
        profileBatchCache = preloadProfiles(List.of(e));
        try {
            return toResponseFromCache(e);
        } finally {
            profileBatchCache = Map.of();
        }
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public SaseEslestirmeResponseDto apiOlustur(SaseEslestirmeRequestDto dto, UUID userId) {
        validateRequest(dto);
        SaseEslestirme e = mapNewFromDto(dto, userId);
        SaseEslestirme saved = self.persist(e);
        return apiTekil(userId, saved.getId());
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public SaseEslestirmeResponseDto apiGuncelle(UUID userId, UUID id, SaseEslestirmeRequestDto dto) {
        validateRequest(dto);
        SaseEslestirme e = idIleGetir(id);
        ownership(e, userId);
        mergeFromDto(e, dto);
        self.persist(e);
        return apiTekil(userId, id);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void apiSil(UUID userId, UUID id) {
        int n = saseEslestirmeRepository.deleteByIdAndUserId(id, userId);
        if (n == 0) {
            throw new EntityNotFoundException("Kayıt bulunamadı veya size ait değil.");
        }
    }

    @Transactional
    public int apiCalistirHepsini(UUID userId) {
        return saseEslestirmeRepository.calistirHepsiniKullaniciyaGore(userId);
    }

    @Transactional
    public void apiCalistirTek(UUID userId, UUID id) {
        int n = saseEslestirmeRepository.calistirByIdVeKullanici(id, userId);
        if (n == 0) {
            throw new EntityNotFoundException("Çalıştırılacak kayıt bulunamadı veya size ait değil.");
        }
    }

    public SaseEslestirme idIleGetir(UUID id) {
        return saseEslestirmeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Şase eşleştirme kaydı bulunamadı: " + id));
    }

    /**
     * VW formül liste sayfası — kullanıcının kaydettiği tüm VW formülleri (aynı şase tablosundan).
     */
    public List<SaseVwListRowDto> vwFormulListeSatirlari(UUID userId) {
        List<SaseEslestirme> list = saseEslestirmeRepository.findByUserIdOrderByCreatedAtAsc(userId);
        if (list.isEmpty()) {
            return List.of();
        }
        Map<UUID, Profile> profs = preloadProfiles(list);
        return list.stream()
                .map(e -> toVwListRowDto(e, profs))
                .toList();
    }

    public List<String> vwFormulListeEkleyenSecenekleri(UUID userId) {
        return vwFormulListeSatirlari(userId).stream()
                .map(SaseVwListRowDto::ekleyenKullanici)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void apiVwListeAlanTemizle(UUID userId, UUID id) {
        SaseEslestirme e = idIleGetir(id);
        ownership(e, userId);
        e.setBaslik("(başlık girin)");
        e.setStokListe(new UUID[0]);
        e.setSaseKod1(null);
        e.setSaseKod2(null);
        e.setSaseKod3(null);
        e.setSaseKod4(null);
        e.setSaseKod5(null);
        e.setSaseKod6(null);
        e.setSaseKod7(null);
        e.setSaseKod8(null);
        e.setSaseKod9(null);
        e.setModel(null);
        e.setUretimTarihiBaslangic(null);
        e.setUretimTarihiBitis(null);
        e.setModelYili(null);
        e.setSatisTipi(null);
        e.setMotorKodu(null);
        e.setSanzimanKodu(null);
        e.setAksTahrigiTanimi(null);
        e.setEksDonanim(null);
        e.setExecuted(false);
        e.setExecutedDate(null);
        e.setEslenikSaseAdedi(null);
        e.setDegerliAciklamaStokKods(null);
        saseEslestirmeRepository.save(e);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public SaseEslestirmeResponseDto apiFormulKopyala(UUID userId, UUID kaynakId) {
        SaseEslestirme src = idIleGetir(kaynakId);
        ownership(src, userId);
        SaseEslestirme copy = duplicateForFormulKopyasi(src, userId);
        SaseEslestirme saved = self.persist(copy);
        return apiTekil(userId, saved.getId());
    }

    private SaseVwListRowDto toVwListRowDto(SaseEslestirme e, Map<UUID, Profile> profs) {
        Profile p = e.getUserId() != null ? profs.get(e.getUserId()) : null;
        String ekleyen = "";
        if (p != null) {
            if (p.getAdSoyad() != null && !p.getAdSoyad().isBlank()) {
                ekleyen = p.getAdSoyad().trim();
            } else if (p.getKullaniciAdi() != null && !p.getKullaniciAdi().isBlank()) {
                ekleyen = p.getKullaniciAdi().trim();
            }
        }
        List<StokOzetDto> stk = stokDetey(e.getStokListe());
        String stokCsv = stk.stream().map(StokOzetDto::stokKodu).filter(k -> k != null && !k.isBlank()).collect(Collectors.joining(","));
        LocalDate execDay = null;
        if (e.getExecutedDate() != null) {
            execDay = e.getExecutedDate().toLocalDate();
        }
        return new SaseVwListRowDto(
                e.getId(),
                ekleyen.isBlank() ? "–" : ekleyen,
                e.getBaslik(),
                stokCsv.isBlank() ? "" : stokCsv,
                e.getSaseKod1(), e.getSaseKod2(), e.getSaseKod3(), e.getSaseKod4(),
                e.getSaseKod5(), e.getSaseKod6(), e.getSaseKod7(), e.getSaseKod8(), e.getSaseKod9(),
                null,
                e.getModel(),
                e.getUretimTarihiBaslangic(),
                e.getUretimTarihiBitis(),
                e.getModelYili(),
                e.getSatisTipi(),
                e.getMotorKodu(),
                e.getSanzimanKodu(),
                e.getAksTahrigiTanimi(),
                e.getEksDonanim(),
                e.getExecuted(),
                execDay,
                e.getEslenikSaseAdedi(),
                e.getDegerliAciklamaStokKods()
        );
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public SaseEslestirme persist(SaseEslestirme eslestirme) {
        return saseEslestirmeRepository.save(eslestirme);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public SaseEslestirme kaydet(SaseEslestirme eslestirme) {
        return saseEslestirmeRepository.save(eslestirme);
    }

    @Transactional
    public SaseEslestirme calistir(UUID id, UUID kullaniciId) {
        int u = saseEslestirmeRepository.calistirByIdVeKullanici(id, kullaniciId);
        if (u == 0) {
            throw new EntityNotFoundException("Kayıt çalıştırılamadı.");
        }
        return idIleGetir(id);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void silGuvenli(UUID id, UUID userId) {
        int n = saseEslestirmeRepository.deleteByIdAndUserId(id, userId);
        if (n == 0) {
            throw new EntityNotFoundException("Kayıt silinemedi veya size ait değil.");
        }
    }

    private int sanitizePageSize(int requestedSize) {
        if (requestedSize <= 0) {
            return 25;
        }
        return Math.min(requestedSize, MAX_PAGE_SIZE);
    }

    private static void ownership(SaseEslestirme e, UUID userId) {
        if (e.getUserId() == null || !e.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Kayıt bulunamadı.");
        }
    }

    private void validateRequest(SaseEslestirmeRequestDto dto) {
        SaseAlanValidasyonlari.saseGrupVirgulluKontrol("Şase kodu grup 3", dto.saseKod3(), 5);
        SaseAlanValidasyonlari.saseGrupVirgulluKontrol("Şase kodu grup 5", dto.saseKod5(), 5);
        SaseAlanValidasyonlari.saseKod6Kontrol(dto.saseKod6());
    }

    private SaseEslestirme mapNewFromDto(SaseEslestirmeRequestDto d, UUID userId) {
        return SaseEslestirme.builder()
                .userId(userId)
                .baslik(d.baslik().trim())
                .stokListe(nonEmptyUuidArray(d.stokListeNormalized()))
                .degerliAciklamaStokKods(blankNull(d.degerliAciklamaStokKods()))
                .saseKod1(blankNull(d.saseKod1()))
                .saseKod2(blankNull(d.saseKod2()))
                .saseKod3(blankNull(d.saseKod3()))
                .saseKod4(blankNull(d.saseKod4()))
                .saseKod5(blankNull(d.saseKod5()))
                .saseKod6(blankNull(d.saseKod6()))
                .saseKod7(blankNull(d.saseKod7()))
                .saseKod8(blankNull(d.saseKod8()))
                .saseKod9(blankNull(d.saseKod9()))
                .model(blankNull(d.model()))
                .modelYili(blankNull(d.modelYili()))
                .uretimTarihiBaslangic(d.uretimTarihiBaslangic())
                .uretimTarihiBitis(d.uretimTarihiBitis())
                .satisTipi(blankNull(d.satisTipi()))
                .motorKodu(blankNull(d.motorKodu()))
                .sanzimanKodu(blankNull(d.sanzimanKodu()))
                .aksTahrigiTanimi(blankNull(d.aksTahrigiTanimi()))
                .eksDonanim(blankNull(d.eksDonanim()))
                .executed(false)
                .eslenikSaseAdedi(0)
                .build();
    }

    /** Yeni kayıt; çalıştırma bilgisi sıfırlanır, başlığa tek seferlik " — kopya" eklenir. */
    private SaseEslestirme duplicateForFormulKopyasi(SaseEslestirme src, UUID userId) {
        String raw = src.getBaslik() != null ? src.getBaslik().trim() : "";
        if (raw.isEmpty()) {
            raw = "(başlık girin)";
        }
        String baslik = baslikIcinKopyaSuffix(raw);

        UUID[] stokSrc = src.getStokListe();
        UUID[] stokCopy = stokSrc == null ? new UUID[0] : Arrays.copyOf(stokSrc, stokSrc.length);

        return SaseEslestirme.builder()
                .userId(userId)
                .baslik(baslik)
                .stokListe(stokCopy)
                .degerliAciklamaStokKods(blankNull(src.getDegerliAciklamaStokKods()))
                .saseKod1(blankNull(src.getSaseKod1()))
                .saseKod2(blankNull(src.getSaseKod2()))
                .saseKod3(blankNull(src.getSaseKod3()))
                .saseKod4(blankNull(src.getSaseKod4()))
                .saseKod5(blankNull(src.getSaseKod5()))
                .saseKod6(blankNull(src.getSaseKod6()))
                .saseKod7(blankNull(src.getSaseKod7()))
                .saseKod8(blankNull(src.getSaseKod8()))
                .saseKod9(blankNull(src.getSaseKod9()))
                .model(blankNull(src.getModel()))
                .modelYili(blankNull(src.getModelYili()))
                .uretimTarihiBaslangic(src.getUretimTarihiBaslangic())
                .uretimTarihiBitis(src.getUretimTarihiBitis())
                .satisTipi(blankNull(src.getSatisTipi()))
                .motorKodu(blankNull(src.getMotorKodu()))
                .sanzimanKodu(blankNull(src.getSanzimanKodu()))
                .aksTahrigiTanimi(blankNull(src.getAksTahrigiTanimi()))
                .eksDonanim(blankNull(src.getEksDonanim()))
                .executed(false)
                .executedDate(null)
                .eslenikSaseAdedi(0)
                .build();
    }

    /** Var olan başlıkta " — kopya" yoksa sonuna eklenir (tekrarsız). */
    private static String baslikIcinKopyaSuffix(String baslikTrimmed) {
        String suffix = " — kopya";
        String lower = baslikTrimmed.toLowerCase(Locale.ROOT);
        if (lower.endsWith(suffix.toLowerCase(Locale.ROOT))) {
            return baslikTrimmed;
        }
        return baslikTrimmed + suffix;
    }

    private void mergeFromDto(SaseEslestirme e, SaseEslestirmeRequestDto d) {
        e.setBaslik(d.baslik().trim());
        e.setStokListe(nonEmptyUuidArray(d.stokListeNormalized()));
        e.setDegerliAciklamaStokKods(blankNull(d.degerliAciklamaStokKods()));
        e.setSaseKod1(blankNull(d.saseKod1()));
        e.setSaseKod2(blankNull(d.saseKod2()));
        e.setSaseKod3(blankNull(d.saseKod3()));
        e.setSaseKod4(blankNull(d.saseKod4()));
        e.setSaseKod5(blankNull(d.saseKod5()));
        e.setSaseKod6(blankNull(d.saseKod6()));
        e.setSaseKod7(blankNull(d.saseKod7()));
        e.setSaseKod8(blankNull(d.saseKod8()));
        e.setSaseKod9(blankNull(d.saseKod9()));
        e.setModel(blankNull(d.model()));
        e.setModelYili(blankNull(d.modelYili()));
        e.setUretimTarihiBaslangic(d.uretimTarihiBaslangic());
        e.setUretimTarihiBitis(d.uretimTarihiBitis());
        e.setSatisTipi(blankNull(d.satisTipi()));
        e.setMotorKodu(blankNull(d.motorKodu()));
        e.setSanzimanKodu(blankNull(d.sanzimanKodu()));
        e.setAksTahrigiTanimi(blankNull(d.aksTahrigiTanimi()));
        e.setEksDonanim(blankNull(d.eksDonanim()));
    }

    private static UUID[] nonEmptyUuidArray(UUID[] raw) {
        if (raw == null || raw.length == 0) {
            return new UUID[0];
        }
        List<UUID> filtered = Arrays.stream(raw)
                .filter(Objects::nonNull)
                .toList();
        if (filtered.isEmpty()) {
            return new UUID[0];
        }
        return filtered.toArray(UUID[]::new);
    }

    private static String blankNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    /** DB {@code timestamp} → API'de ISO offset ile (UTC varsayımı). */
    private static OffsetDateTime dbTimestampToApi(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atOffset(ZoneOffset.UTC);
    }

    private SaseEslestirmeResponseDto toResponseFromCache(SaseEslestirme e) {
        Profile p = e.getUserId() != null ? profileBatchCache.get(e.getUserId()) : null;
        if (p == null && e.getUserId() != null) {
            p = profileRepository.findById(e.getUserId()).orElse(null);
        }
        return toResponseDto(e, p, stokDetey(e.getStokListe()));
    }

    private SaseEslestirmeResponseDto toResponseDto(SaseEslestirme e, Profile p, List<StokOzetDto> stocks) {
        UUID[] stk = e.getStokListe() != null ? e.getStokListe() : new UUID[0];
        return new SaseEslestirmeResponseDto(
                e.getId(),
                e.getUserId(),
                p != null ? p.getKullaniciAdi() : null,
                p != null ? p.getAdSoyad() : null,
                e.getBaslik(),
                stk,
                stocks,
                e.getSaseKod1(), e.getSaseKod2(), e.getSaseKod3(), e.getSaseKod4(),
                e.getSaseKod5(), e.getSaseKod6(), e.getSaseKod7(), e.getSaseKod8(), e.getSaseKod9(),
                e.getModel(),
                e.getUretimTarihiBaslangic(), e.getUretimTarihiBitis(),
                e.getModelYili(),
                e.getSatisTipi(),
                e.getMotorKodu(),
                e.getSanzimanKodu(),
                e.getAksTahrigiTanimi(),
                e.getEksDonanim(),
                e.getExecuted(),
                dbTimestampToApi(e.getExecutedDate()),
                e.getEslenikSaseAdedi(),
                e.getDegerliAciklamaStokKods(),
                dbTimestampToApi(e.getCreatedAt()),
                dbTimestampToApi(e.getUpdatedAt())
        );
    }

    private Map<UUID, Profile> preloadProfiles(List<SaseEslestirme> list) {
        Set<UUID> ids = list.stream()
                .map(SaseEslestirme::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Profile> profs = profileRepository.findAllById(ids);
        return profs.stream().collect(Collectors.toMap(Profile::getId, p -> p));
    }

    private List<StokOzetDto> stokDetey(UUID[] ids) {
        if (ids == null || ids.length == 0) {
            return List.of();
        }
        List<UUID> idList = new ArrayList<>();
        for (UUID uid : ids) {
            if (uid != null) {
                idList.add(uid);
            }
        }
        if (idList.isEmpty()) {
            return List.of();
        }
        List<Stok> liste = stokRepository.findAllById(idList);
        Map<UUID, Stok> m = liste.stream().collect(Collectors.toMap(Stok::getId, s -> s));
        List<StokOzetDto> out = new ArrayList<>();
        for (UUID uid : idList) {
            Stok s = m.get(uid);
            if (s != null) {
                out.add(new StokOzetDto(s.getId(), s.getStokKodu(), s.getStokAdi()));
            }
        }
        return out;
    }

    private Specification<SaseEslestirme> buildListSpec(UUID userId, Map<String, String> raw) {
        Specification<SaseEslestirme> spec =
                Specification.where((root, q, cb) -> cb.equal(root.get("userId"), userId));

        String exec = cleaned(raw.get("executed"));
        if (!exec.isEmpty()) {
            boolean b = Boolean.parseBoolean(exec);
            spec = spec.and((root, q, cb) -> cb.equal(root.get("executed"), b));
        }

        for (Map.Entry<String, String> entry : raw.entrySet()) {
            String key = entry.getKey();
            if ("executed".equals(key) || !LIST_FILTER_FIELDS.contains(key)) {
                continue;
            }
            String needleText = cleaned(entry.getValue());
            if (needleText.isEmpty()) {
                continue;
            }
            final String field = key;
            String pattern = "%" + needleText.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get(field)), pattern));
        }
        return spec;
    }

    private static String cleaned(String s) {
        return s == null ? "" : s.trim();
    }
}

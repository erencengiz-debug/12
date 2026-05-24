package com.sase.app.sasehub;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Marka seçim kutusu (slug) ile stok.tablo marka sütunu eşlemesi — hepsi küçük harf karşılaştırılır.
 */
public final class ManuelAracMarka {

    private ManuelAracMarka() {}

    /**
     * @param slug vw, audi, seat, cupra, skoda, peugeot, ds, citroen, opel, fiat
     * @return veritabanında aranacak küçük harf marka değişkenleri
     */
    public static List<String> markaVariantsForStok(String slug) {
        if (!StringUtils.hasText(slug)) {
            return List.of();
        }
        String s = slug.trim().toLowerCase(Locale.ROOT);
        // UTF-8 normalizasyon: kullanıcı "citroen" yazar → citroën de dene
        return switch (s) {
            case "vw", "volkswagen" -> variants("vw", "volkswagen");
            case "audi" -> variants("audi");
            case "seat" -> variants("seat");
            case "cupra" -> variants("cupra");
            case "skoda" -> variants("skoda");
            case "peugeot" -> variants("peugeot");
            case "ds" -> variants("ds");
            case "citroen", "citroën" -> variants("citroen", "citroën");
            case "opel" -> variants("opel");
            case "fiat" -> variants("fiat");
            default -> variants(s);
        };
    }

    private static List<String> variants(String... parts) {
        List<String> o = new ArrayList<>();
        for (String p : parts) {
            if (StringUtils.hasText(p)) {
                String t = p.trim().toLowerCase(Locale.ROOT);
                if (!o.contains(t)) {
                    o.add(t);
                }
            }
        }
        return List.copyOf(o);
    }

    /** Stok seçim kutusu aktif olduğunda yönlendirilebilecek formül/marka kök yolu */
    public static String formulYolu(String slug) {
        if (!StringUtils.hasText(slug)) return "/sase";
        String s = slug.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "vw", "audi", "seat", "cupra", "skoda" -> "/sase/vw/formuls";
            case "peugeot", "ds", "citroen", "citroën" -> "/sase/psa";
            case "opel" -> "/sase/opel";
            case "fiat" -> "/sase/fiat";
            default -> "/sase";
        };
    }
}

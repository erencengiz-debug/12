package com.sase.app.service;

/**
 * ASP mantığına yakın doğrulamalar — virgüllü alanlar ve grupo 6 numerik kontrolü.
 */
public final class SaseAlanValidasyonlari {

    private static final char LATIN_SMALL_E_ACUTE = '\u00e9';
    /** İki parçalı kod6 değeri (örn. iki kısa alan): "123456::654321¤KR:BUYUK" */
    private static final String SAS_KOD_6_DUAL_DELIM = "::";
    /** Kriter soneki (frontend-VW uyumu). */
    private static final java.util.regex.Pattern KRITER_TAIL =
            java.util.regex.Pattern.compile("\u00A4KR:.+$"); // ¤KR:...

    private SaseAlanValidasyonlari() {
    }

    /** Grup3 / Grup5: virgüllü listelerde é ve virgül birlikte olamaz (çoklu giriş + é kuralı). */
    public static void saseGrupVirgulluKontrol(String alanEtiketi, String deger, int maxSegment) {
        if (deger == null || deger.isBlank()) {
            return;
        }
        if (deger.indexOf(',') >= 0 && deger.indexOf(LATIN_SMALL_E_ACUTE) >= 0) {
            throw new IllegalArgumentException(
                    alanEtiketi + ": Virgülle ayrılmış girişlerde é karakterine izin verilmez.");
        }
        int segments = commaSegments(deger);
        if (segments > maxSegment) {
            throw new IllegalArgumentException(
                    alanEtiketi + ": En fazla " + maxSegment + " virgülle ayrılmış parça kullanılabilir.");
        }
    }

    public static void saseKod6Kontrol(String deger) {
        if (deger == null || deger.isBlank()) {
            return;
        }
        String trimmed = deger.trim();
        String withoutKriter = stripKriterSuffix(trimmed);
        if (!withoutKriter.equals(trimmed)) {
            trimmed = withoutKriter;
        }
        if (trimmed.contains(SAS_KOD_6_DUAL_DELIM)) {
            String[] pair = trimmed.split(SAS_KOD_6_DUAL_DELIM, -1);
            if (pair.length != 2) {
                throw new IllegalArgumentException("6. grup (sase kod 6): İki sayısal alan yanlış biçimlendirildi.");
            }
            kod6TekSayiyiDogrula(pair[0], "ilk");
            kod6TekSayiyiDogrula(pair[1], "ikinci");
            return;
        }
        kod6TekSayiyiDogrula(trimmed, "");
    }

    private static String stripKriterSuffix(String trimmed) {
        return KRITER_TAIL.matcher(trimmed).replaceFirst("").trim();
    }

    private static void kod6TekSayiyiDogrula(String raw, String parcaEtiketi) {
        String s = raw == null ? "" : raw.trim();
        if (s.isBlank()) {
            return;
        }
        if (!s.matches("^\\d+$")) {
            String ek = parcaEtiketi.isBlank() ? "" : (" (" + parcaEtiketi + " alan)");
            throw new IllegalArgumentException("6. grup (sase kod 6)" + ek + ": Sadece sayı girilebilir (0 ile 999.999).");
        }
        long val = Long.parseLong(s);
        if (val < 0 || val > 999_999L) {
            String ek = parcaEtiketi.isBlank() ? "" : (" (" + parcaEtiketi + " alan)");
            throw new IllegalArgumentException("6. grup" + ek + ": Değer 0 ile 999.999 arasında olmalıdır.");
        }
    }

    private static int commaSegments(String s) {
        String[] tok = s.split(",", -1);
        int nonEmpty = 0;
        for (String t : tok) {
            if (t != null && !t.trim().isEmpty()) {
                nonEmpty++;
            }
        }
        return Math.max(nonEmpty, 1);
    }
}

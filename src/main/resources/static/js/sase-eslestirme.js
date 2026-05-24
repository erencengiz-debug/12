/**
 * Şase eşleştirme tam sayfası (/sase/{marka}; VW için /sase/vw/formuls); cookie tabanlı oturum.
 */
(function () {
    const MARKA = window.__PAGE_MARKA || "VW";
    const LEGACY_VW = !!window.__SASE_LEGACY_VW;
    const PSA_FAMILY = MARKA === "PSA" || MARKA === "OPEL";

    const API_LIST = "/api/sase";

    /** @type {string|null} */
    let editingId = null;

    /** @type {{totalPages:number,page:number,size:number,totalCount:number}} */
    let pagingMeta = { totalPages: 1, page: 0, size: 25, totalCount: 0 };

    /** @type {Array<{id:(string|null),stokKodu:string}>} */
    let selectedStocks = [];

    /** Son sunucudan gelen liste */
    let lastRows = [];

    function gv(id) {
        const el = document.getElementById(id);
        return el && "value" in el ? /** @type {HTMLInputElement} */(el).value.trim() : "";
    }

    function sv(id, val) {
        const el = document.getElementById(id);
        if (el && "value" in el) /** @type {HTMLInputElement} */(el).value = val == null ? "" : String(val);
    }

    function nullIfBlank(s) {
        const t = s == null ? "" : String(s).trim();
        return t === "" ? null : t;
    }

    function toast(msg, ok = true) {
        const holder = document.getElementById("toastStack");
        if (!holder) {
            alert(msg);
            return;
        }
        const id = "t-" + Date.now();
        holder.insertAdjacentHTML("beforeend", `
<div id="${id}" class="toast align-items-center ${ok ? "text-bg-success" : "text-bg-danger"} border-0" role="alert">
  <div class="d-flex">
    <div class="toast-body small">${escapeHtml(msg)}</div>
    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
  </div>
</div>`);
        const el = document.getElementById(id);
        const t = bootstrap.Toast.getOrCreateInstance(el, { delay: 4500 });
        t.show();
        el.addEventListener("hidden.bs.toast", () => el.remove());
    }

    /** @returns {HTMLElement|null} */
    function legacyVwShell() {
        return document.getElementById("legacyVwShell");
    }

    function escapeHtml(t) {
        const map = { "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" };
        return String(t).replace(/[&<>'"]/g, c => /** @type {Record<string,string>} */ (map)[c] || c);
    }

    /** Stok API / DTO yalnızca geçerli UUID string kabul eder; aksi JSON ayrıştırma patlayabilir */
    /** @param {unknown[]} ids */
    function sanitizeStokUuidList(ids) {
        const re = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
        return (Array.isArray(ids) ? ids : [])
            .filter(id => typeof id === "string" && re.test(String(id).trim()));
    }

    async function apiJson(method, url, body) {
        /** @type {RequestInit} */
        const opts = {
            method,
            credentials: "include",
            headers: body != null ? { "Content-Type": "application/json" } : {},
        };
        if (body != null) opts.body = JSON.stringify(body);

        const res = await fetch(url, opts);
        const text = await res.text();
        let j = /** @type {{success?:boolean,message?:string,data?:unknown}|null} */ (null);
        if (text && text.trim()) {
            try {
                j = JSON.parse(text);
            } catch (parseErr) {
                console.warn("API yanıt JSON değil:", method, url, res.status, text.slice(0, 500));
                throw new Error("Sunucu yanıtı okunamadı (HTTP " + res.status + ").");
            }
        }
        if (!j || typeof j.success !== "boolean") {
            console.warn("API beklenmeyen gövde:", method, url, res.status, text?.slice?.(0, 500));
            throw new Error("Beklenmeyen sunucu yanıtı (HTTP " + res.status + ").");
        }
        if (!j.success) {
            console.warn("API hata:", method, url, res.status, j);
            const base = j.message || "İşlem başarısız";
            throw new Error(res.status ? base + " (HTTP " + res.status + ")" : base);
        }
        return j.data;
    }

    function debounce(fn, ms) {
        let timer = 0;
        return function (...args) {
            clearTimeout(timer);
            timer = /** @type {any} */(setTimeout(() => fn.apply(null, args), ms));
        };
    }

    function vwStokListeTextareasiniSenkronizeEt() {
        const ta = document.getElementById("fStokKodListe");
        if (!ta || !("value" in ta)) return;
        if (!selectedStocks.length) {
            /** @type {HTMLInputElement} */ (ta).value = "";
            return;
        }
        /** @type {HTMLInputElement} */ (ta).value =
            "," + selectedStocks.map(s => String(s.stokKodu ?? "").trim()).join(",") + ",";
    }

    function syncStokMetinAlan() {
        if (LEGACY_VW && legacyVwShell()) vwStokListeTextareasiniSenkronizeEt();
        else klasikSyncStokMetinAlan();

    }

    /** Klasik (textarea satır bazlı kod listesi). */
    function klasikSyncStokMetinAlan() {
        const ta = document.getElementById("fStokKodListe");
        if (!ta || !("value" in ta)) return;
        /** @type {HTMLTextAreaElement} */ (ta).value = selectedStocks.map(s => s.stokKodu).join("\n");
    }

    function clearSecilenStoklar() {
        selectedStocks = [];
        syncStokMetinAlan();
    }

    /** @param {{id:string,stokKodu:string}} s */
    function attachStok(s) {
        if (!selectedStocks.some(x => x.id === s.id)) {
            selectedStocks.push({ id: s.id, stokKodu: s.stokKodu });
            syncStokMetinAlan();
        }
    }

    function tarihAlan(inpId, dtoVal) {
        if (!dtoVal) {
            sv(inpId, "");
            return;
        }
        const iso = dtoVal.includes("T") ? dtoVal.split("T")[0] : String(dtoVal).substring(0, 10);
        sv(inpId, iso || "");
    }

    /** @param {string|undefined|null} m */
    function oprParseModelYili(m) {
        if (!m || typeof m !== "string") return { core: "", b: "", e: "" };
        const match = /^(.*?)\s*\[(\d+)-(\d+)\]\s*$/.exec(m.trim());
        if (!match) return { core: m.trim(), b: "", e: "" };
        return { core: match[1].trim(), b: match[2], e: match[3] };
    }

    const VW_SPLIT_PIN = "\u00A7";
    /** @typedef {{olmayan:string, degerli:string, krit:Record<string,string>}} ParsedVwMeta */

    /** @returns {HTMLElement|null} */
    function kritSel(id) {
        return document.getElementById(id);
    }

    /** @param {HTMLElement|null} el */
    function selVal(el, v) {
        if (!el || !("value" in el)) return;
        /** @type {HTMLSelectElement} */ (el).value = v || "";
    }

    function kritleriYukleVw(meta) {
        const k = meta.krit || {};
        selVal(kritSel("vwK_g4"), k.g4 || "");
        selVal(kritSel("vwK_g6"), k.g6 || "");
        selVal(kritSel("vwK_model"), k.model || "");
        selVal(kritSel("vwK_uret"), k.uret || "");
        selVal(kritSel("vwK_modelYili"), k.modelYili || "");
        selVal(kritSel("vwK_satisTipi"), k.satisTipi || "");
        selVal(kritSel("vwK_motor"), k.motor || "");
        selVal(kritSel("vwK_sanziman"), k.sanziman || "");
        selVal(kritSel("vwK_aks"), k.aks || "");
        selVal(kritSel("vwK_eks"), k.eks || "");
    }

    function kritleriAlVwUi() {
        /** @type {Record<string,string>} */
        const o = {};
        const picks = /** @type {const} */ ([
            ["g4", "vwK_g4"],
            ["g6", "vwK_g6"],
            ["model", "vwK_model"],
            ["uret", "vwK_uret"],
            ["modelYili", "vwK_modelYili"],
            ["satisTipi", "vwK_satisTipi"],
            ["motor", "vwK_motor"],
            ["sanziman", "vwK_sanziman"],
            ["aks", "vwK_aks"],
            ["eks", "vwK_eks"],
        ]);
        for (const [key, sid] of picks) {
            const el = /** @type {HTMLSelectElement|null} */ (document.getElementById(sid));
            if (el?.value?.trim()) o[key] = el.value.trim();
        }
        return o;
    }


    const META_PRE = "<<<SASE_VW_META_v1>>>";
    const META_SUF = "<<<END_META>>>";

    /** @param {string|null|undefined} raw */
    function parseVwDegerAlan(raw) {
        const s = raw == null ? "" : String(raw);
        if (!s.startsWith(META_PRE)) {
            return /** @type {ParsedVwMeta} */ ({
                olmayan: "",
                degerli: s.trim(),
                krit: {},
            });
        }
        const j0 = META_PRE.length;
        const jEnd = s.indexOf(META_SUF, j0);
        if (jEnd < 0) {
            return { olmayan: "", degerli: "", krit: {} };
        }
        try {
            /** @type {any} */
            const j = JSON.parse(s.slice(j0, jEnd));
            return {
                olmayan: String(j.olmayan ?? ""),
                degerli: String(j.degerli ?? ""),
                krit: typeof j.krit === "object" && j.krit !== null ? j.krit : {},
            };
        } catch (_) {
            return { olmayan: "", degerli: "", krit: {} };
        }
    }

    /** @param {ParsedVwMeta} m */
    function buildVwDegerAlan(m) {
        const payload = JSON.stringify({
            olmayan: m.olmayan || "",
            degerli: m.degerli || "",
            krit: m.krit || {},
        });
        return META_PRE + payload + META_SUF;
    }

    /** @param {string|null|undefined} raw */
    function parseSaseKod4Db(raw) {
        if (!raw || !String(raw).trim()) return { a: "", b: "", k: "" };
        let s = String(raw).trim();
        let kr = "";
        const km = /\u00A4KR:([A-Z_]+)$/.exec(s);
        if (km) {
            kr = km[1];
            s = s.replace(/\u00A4KR:[A-Z_]+$/, "").trim();
        }
        const eq = s.indexOf("=");
        if (eq < 0) return { a: s, b: "", k: kr };
        return { a: s.slice(0, eq).trim(), b: s.slice(eq + 1).trim(), k: kr };
    }

    /** @returns {null|string} */
    function encodeSaseKod4Ui() {
        const a = gv("vwG4a");
        const b = gv("vwG4b");
        const ks = /** @type {HTMLSelectElement|null} */ (document.getElementById("vwK_g4"));
        const kk = ks?.value?.trim() || "";
        const core = (a || b) ? a + "=" + b : "";
        if (!core && !kk) return null;
        return kk ? core + "\u00A4KR:" + kk : core;
    }

    /** @param {string|null|undefined} raw */
    function parseSaseKod6Db(raw) {
        if (!raw || !String(raw).trim()) return { a: "", b: "", k: "" };
        let s = String(raw).trim();
        let kr = "";
        const mm = /\u00A4KR:([A-Z_]+)$/.exec(s);
        if (mm) {
            kr = mm[1];
            s = s.replace(/\u00A4KR:[A-Z_]+$/, "").trim();
        }
        if (s.includes("::")) {
            const p = s.split("::", 2);
            return { a: (p[0] || "").trim(), b: (p[1] || "").trim(), k: kr };
        }
        return { a: s.trim(), b: "", k: kr };
    }

    /** @returns {null|string} */
    function encodeSaseKod6Ui() {
        const a = gv("vwG6a");
        const b = gv("vwG6b");
        const ks = /** @type {HTMLSelectElement|null} */ (document.getElementById("vwK_g6"));
        const kk = ks?.value?.trim() || "";
        if (!a && !b) return null;
        if (a && b) return a.trim() + "::" + b.trim() + (kk ? "\u00A4KR:" + kk : "");
        const one = (a || b).trim();
        return kk ? one + "\u00A4KR:" + kk : one;
    }

    /** @param {string[]} parts */
    function packPin789(parts) {
        const p = parts.map(x => String(x ?? "").trim());
        while (p.length < 9) p.push("");
        /** @type {Record<string,string|null>} */
        const kod = {};
        for (let r = 0; r < 3; r++) {
            const triple = [p[r * 3], p[r * 3 + 1], p[r * 3 + 2]];
            if (triple.every(t => !t)) {
                kod["saseKod" + (7 + r)] = null;
            } else {
                kod["saseKod" + (7 + r)] = triple.join(VW_SPLIT_PIN);
            }
        }
        return kod;
    }

    /** @returns {string[]} */
    function unpackPin789(k7, k8, k9) {
        /** @type {string[]} */
        const out = [];
        for (const blk of [k7, k8, k9]) {
            const s = String(blk ?? "").trim();
            if (!s || s.indexOf(VW_SPLIT_PIN) < 0) {
                if (s) out.push(s, "", "");
                else out.push("", "", "");
                continue;
            }
            const pts = String(blk).split(VW_SPLIT_PIN);
            while (pts.length < 3) pts.push("");
            out.push(...pts.slice(0, 3));
        }
        while (out.length < 9) out.push("");
        return out.slice(0, 9);
    }

    function virgulleEnFazla5VeEoOlmaz(grupEtiketi, deger, maxPiece) {
        if (!deger || !String(deger).trim()) return;
        const latinE = "\u00e9";
        if (deger.indexOf(",") >= 0 && deger.indexOf(latinE) >= 0) {
            throw new Error(grupEtiketi + ": Virgülle ayrılmış girişlerde é kullanılamaz.");
        }
        const tok = deger.split(",");
        const dolu = tok.filter(x => String(x ?? "").trim() !== "").length || 1;
        if (dolu > maxPiece) throw new Error(grupEtiketi + ": En fazla " + maxPiece + " değer girin.");
    }

    function grup6IkiliSayilar() {
        const a = gv("vwG6a");
        const b = gv("vwG6b");
        if (!a && !b) return;
        const bad = txt => txt && (!/^\d+$/.test(txt) || Number(txt) < 0 || Number(txt) > 999_999);
        if (bad(a) || bad(b))
            throw new Error("6. grup: Sayısal 0–999.999.");
    }

    /**
     * VW form: yalnızca Formül Başlığı zorunlu (sunucu DTO ile uyumlu).
     * Turuncu araç alanları boş bırakılabilir.
     */
    function vwTuruncularKontrol() {
        const shell = legacyVwShell();
        let hata = false;
        if (!shell) return false;
        shell.querySelectorAll(".lv-inv").forEach(el => el.classList.remove("lv-inv"));

        const bas = /** @type {HTMLInputElement|null} */ (document.getElementById("fBaslik"));
        if (bas) {
            if (!gv("fBaslik").trim()) {
                bas.classList.add("lv-inv");
                hata = true;
            } else {
                bas.classList.remove("lv-inv");
            }
        }
        return hata;
    }

    /** @returns {Promise<string[]>} list of UUID hex */
    async function textareaStokKodlarınıCozuml() {
        const raw = document.getElementById("fStokKodListe")?.["value"];
        let t = String(raw ?? "").trim();
        if (!t && selectedStocks.length) vwStokListeTextareasiniSenkronizeEt();
        t = String(document.getElementById("fStokKodListe")?.["value"] ?? "").trim();
        /** @type {string[]} */
        const codes = [];
        if (t) {
            t.split(",").forEach(p => {
                const x = p.trim();
                if (x) codes.push(x);
            });
        }
        const uniq = [...new Set(codes.map(x => x.trim()).filter(Boolean))];
        if (!uniq.length) {
            selectedStocks = [];
            vwStokListeTextareasiniSenkronizeEt();
            return [];
        }
        /** @type {string[]} */
        const ids = [];
        /** @type {{id:string|null,stokKodu:string}[]} */
        const stocks = [];
        for (const kod of uniq) {
            /** @type {any[]} */
            const rows = await apiJson(
                "GET",
                API_LIST + "/stok-ara?q=" + encodeURIComponent(kod) + "&limit=50",
                null
            );
            const kodLow = kod.toLocaleLowerCase("tr-TR");
            const hit =
                rows.find(s => String(s.stokKodu ?? "").trim().toLocaleLowerCase("tr-TR") === kodLow) ||
                null;
            if (hit) {
                ids.push(String(hit.id));
                stocks.push({ id: String(hit.id), stokKodu: kod });
            } else {
                // Veritabanında tam kod yoksa yine de formül kaydedilir; stok listesi boş kalır
                stocks.push({ id: null, stokKodu: kod });
            }
        }
        selectedStocks = stocks;
        vwStokListeTextareasiniSenkronizeEt();
        return ids;
    }

    /** @returns {Promise<any>} */
    async function collectDtoLegacyVw() {
        virgulleEnFazla5VeEoOlmaz("3. grup", gv("vwG3"), 5);
        virgulleEnFazla5VeEoOlmaz("5. grup", gv("vwG5"), 5);
        grup6IkiliSayilar();

        /** @type {string[]} */
        const pins = [];
        for (let i = 1; i <= 9; i++) pins.push(gv("vwPin" + i));
        const pinMap = packPin789(pins);

        /** @type {ParsedVwMeta} */
        const meta = {
            olmayan: gv("vwBlacklist"),
            degerli: gv("vwDegerliStokTek"),
            krit: kritleriAlVwUi(),
        };
        const stokliste = sanitizeStokUuidList(await textareaStokKodlarınıCozuml());

        /** @type {Record<string,any>} */
        const body = Object.assign(pinMap, {
            saseKod1: nullIfBlank(gv("vwG1")),
            saseKod2: nullIfBlank(gv("vwG2")),
            saseKod3: nullIfBlank(gv("vwG3")),
            saseKod4: encodeSaseKod4Ui(),
            saseKod5: nullIfBlank(gv("vwG5")),
            saseKod6: encodeSaseKod6Ui(),
            baslik: gv("fBaslik"),
            stokListe: stokliste,
            model: nullIfBlank(gv("fModel")),
            modelYili: nullIfBlank(gv("fModelYili")),
            uretimTarihiBaslangic: nullIfBlank(gv("fUretBas")) || null,
            uretimTarihiBitis: nullIfBlank(gv("fUretBit")) || null,
            satisTipi: nullIfBlank(gv("fSatisTipi")),
            motorKodu: nullIfBlank(gv("fMotorKodu")),
            sanzimanKodu: nullIfBlank(gv("fSanzimanKodu")),
            aksTahrigiTanimi: nullIfBlank(gv("fAks")),
            eksDonanim: nullIfBlank(gv("fEks")),
            degerliAciklamaStokKods: buildVwDegerAlan(meta),
        });
        return body;
    }

    function fmtIsoOffset(v) {
        if (!v) return "";
        const s = String(v);
        if (s.includes("T")) return s.substring(0, 19).replace("T", " ");
        return s;
    }

    /** @param {any} dto */
    function formuDtoIleDoldurVw(dto) {
        editingId = dto.id || null;

        const roUser = dto.kullaniciAdi
            ? (dto.kullaniciAdi + (dto.adSoyad ? " — " + dto.adSoyad : ""))
            : "";
        sv("vwRoId", dto.id || "");
        sv("vwRoCreated", fmtIsoOffset(dto.createdAt));
        sv("vwRoExecuted", fmtIsoOffset(dto.executedDate));
        sv("vwRoUpdated", fmtIsoOffset(dto.updatedAt));
        sv("vwRoUser", roUser);
        sv("vwRoEsSay", dto.eslenikSaseAdedi != null ? String(dto.eslenikSaseAdedi) : "");

        sv("fBaslik", dto.baslik ?? "");

        sv("vwG1", dto.saseKod1 ?? "");
        sv("vwG2", dto.saseKod2 ?? "");
        sv("vwG3", dto.saseKod3 ?? "");
        const q4 = parseSaseKod4Db(dto.saseKod4);
        sv("vwG4a", q4.a);
        sv("vwG4b", q4.b);
        selVal(document.getElementById("vwK_g4"), q4.k || "");
        sv("vwG5", dto.saseKod5 ?? "");
        const q6 = parseSaseKod6Db(dto.saseKod6);
        sv("vwG6a", q6.a);
        sv("vwG6b", q6.b);
        selVal(document.getElementById("vwK_g6"), q6.k || "");

        const pins = unpackPin789(dto.saseKod7, dto.saseKod8, dto.saseKod9);
        for (let i = 1; i <= 9; i++) sv("vwPin" + i, pins[i - 1] ?? "");

        const meta = parseVwDegerAlan(dto.degerliAciklamaStokKods);
        kritleriYukleVw(meta);
        sv("vwBlacklist", meta.olmayan);
        sv("vwDegerliStokTek", meta.degerli);

        selectedStocks = [];
        if (dto.stokDetaylari && Array.isArray(dto.stokDetaylari)) {
            selectedStocks = dto.stokDetaylari.map(/** @param {any} s */ s => ({
                id: String(s.id),
                stokKodu: String(s.stokKodu ?? ""),
            }));
        }
        vwStokListeTextareasiniSenkronizeEt();

        sv("fModel", dto.model ?? "");
        tarihAlan("fUretBas", dto.uretimTarihiBaslangic);
        tarihAlan("fUretBit", dto.uretimTarihiBitis);
        if (!gv("fUretBas")) sv("fUretBas", "2000-01-01");
        if (!gv("fUretBit")) sv("fUretBit", "2000-01-01");

        sv("fModelYili", dto.modelYili ?? "");
        sv("fSatisTipi", dto.satisTipi ?? "");
        sv("fMotorKodu", dto.motorKodu ?? "");
        sv("fSanzimanKodu", dto.sanzimanKodu ?? "");
        sv("fAks", dto.aksTahrigiTanimi ?? "");
        sv("fEks", dto.eksDonanim ?? "");

        const mod = /** @type {HTMLElement|null} */ (document.getElementById("lblVwKayitMod"));
        if (mod)
            mod.textContent = editingId ? "Kayıtlı düzenleme (güncelleme)" : "Yeni kayıt";
        const btn = /** @type {HTMLElement|null} */ (document.getElementById("btnVwKaydet"));
        if (btn) btn.textContent = editingId ? "Kaydı Güncelle" : "Yeni Kaydı Kaydet";

        [...(legacyVwShell()?.querySelectorAll(".lv-inv") ?? [])].forEach(el =>
            el.classList.remove("lv-inv")
        );
    }

    function temizFormLegacyVw() {
        editingId = null;

        sv("vwRoId", "");
        sv("vwRoCreated", "");
        sv("vwRoExecuted", "");
        sv("vwRoUpdated", "");
        sv("vwRoUser", "");
        sv("vwRoEsSay", "");

        sv("vwG1", "");
        sv("vwG2", "");
        sv("vwG3", "");
        sv("vwG4a", "");
        sv("vwG4b", "");
        sv("vwG5", "");
        sv("vwG6a", "");
        sv("vwG6b", "");
        for (let i = 1; i <= 9; i++) {
            sv("vwPin" + i, "");
        }
        kritleriYukleVw({ olmayan: "", degerli: "", krit: {} });
        sv("vwBlacklist", "");
        sv("vwDegerliStokTek", "");
        sv("fBaslik", "");
        sv("fModel", "");
        sv("fUretBas", "2000-01-01");
        sv("fUretBit", "2000-01-01");
        sv("fModelYili", "");
        sv("fSatisTipi", "");
        sv("fMotorKodu", "");
        sv("fSanzimanKodu", "");
        sv("fAks", "");
        sv("fEks", "");
        clearSecilenStoklar();

        const mod = /** @type {HTMLElement|null} */ (document.getElementById("lblVwKayitMod"));
        if (mod) mod.textContent = "Yeni kayıt";
        const btn = /** @type {HTMLElement|null} */ (document.getElementById("btnVwKaydet"));
        if (btn) btn.textContent = "Yeni Kaydı Kaydet";

        legacyVwShell()?.querySelectorAll(".lv-inv").forEach(el => el.classList.remove("lv-inv"));
    }
    function collectDto() {
        /** @type {Record<string,null|string|string[]>} */
        const kodlar = {};
        for (let i = 1; i <= 9; i++) {
            kodlar["saseKod" + i] = nullIfBlank(document.getElementById("fSaseKod" + i)?.value ?? "");
        }
        /** @type {Record<string,null|string|string[]>} */
        const body = Object.assign(kodlar, {
            stokListe: sanitizeStokUuidList(
                selectedStocks.map(s => s.id).filter(/** @param {any} id */ id => id != null && id !== "")
            ),
            baslik: gv("fBaslik"),
            degerliAciklamaStokKods: nullIfBlank(gv("fDegerliAciklama")),
        });

        if (PSA_FAMILY) {
            const oprB = gv("fPsaOprBas");
            const oprE = gv("fPsaOprBit");
            let my = gv("fPsaOpr");
            if (oprB || oprE) {
                my += (my ? " " : "") + "[" + (oprB || "") + "-" + (oprE || "") + "]";
            }
            body.eksDonanim = nullIfBlank(gv("fPsaUrunGam"));
            body.modelYili = nullIfBlank(my);
            body.aksTahrigiTanimi = nullIfBlank(gv("fPsaAktarma"));
            body.uretimTarihiBaslangic = nullIfBlank(gv("fPsaGarantBas")) || null;
            body.uretimTarihiBitis = nullIfBlank(gv("fPsaGarantBit")) || null;
            body.model = null;
            body.satisTipi = null;
            body.motorKodu = null;
            body.sanzimanKodu = null;
            return body;
        }

        body.model = nullIfBlank(gv("fModel"));
        body.modelYili = nullIfBlank(gv("fModelYili"));
        body.uretimTarihiBaslangic = nullIfBlank(gv("fUretBas")) || null;
        body.uretimTarihiBitis = nullIfBlank(gv("fUretBit")) || null;
        body.satisTipi = nullIfBlank(gv("fSatisTipi"));
        body.motorKodu = nullIfBlank(gv("fMotorKodu"));
        body.sanzimanKodu = nullIfBlank(gv("fSanzimanKodu"));
        body.aksTahrigiTanimi = nullIfBlank(gv("fAks"));
        body.eksDonanim = nullIfBlank(gv("fEks"));
        return body;
    }

    /** @param {any} dto */
    function formuDtoIleDoldur(dto) {
        if (LEGACY_VW && legacyVwShell()) {
            formuDtoIleDoldurVw(dto);
            return;
        }

        editingId = dto.id || null;

        const btnKaydet = document.getElementById("btnKaydet");
        if (btnKaydet) {
            btnKaydet.innerHTML = editingId
                ? '<i class="bi bi-save me-1"></i>Güncelle'
                : '<i class="bi bi-save me-1"></i>Kaydet';
        }

        sv("fBaslik", dto.baslik);
        sv("fDegerliAciklama", dto.degerliAciklamaStokKods ?? "");
        for (let i = 1; i <= 9; i++) sv("fSaseKod" + i, dto["saseKod" + i] ?? "");

        selectedStocks = [];
        if (dto.stokDetaylari && Array.isArray(dto.stokDetaylari)) {
            selectedStocks = dto.stokDetaylari.map(/** @param {any} s */ s => ({
                id: String(s.id),
                stokKodu: String(s.stokKodu ?? ""),
            }));
        }
        syncStokMetinAlan();

        if (PSA_FAMILY) {
            sv("fPsaUrunGam", dto.eksDonanim ?? "");
            const parsed = oprParseModelYili(dto.modelYili ?? "");
            sv("fPsaOpr", parsed.core);
            sv("fPsaOprBas", parsed.b);
            sv("fPsaOprBit", parsed.e);
            sv("fPsaAktarma", dto.aksTahrigiTanimi ?? "");
            tarihAlan("fPsaGarantBas", dto.uretimTarihiBaslangic);
            tarihAlan("fPsaGarantBit", dto.uretimTarihiBitis);
            return;
        }

        sv("fModel", dto.model ?? "");
        sv("fModelYili", dto.modelYili ?? "");
        tarihAlan("fUretBas", dto.uretimTarihiBaslangic);
        tarihAlan("fUretBit", dto.uretimTarihiBitis);
        sv("fSatisTipi", dto.satisTipi ?? "");
        sv("fMotorKodu", dto.motorKodu ?? "");
        sv("fSanzimanKodu", dto.sanzimanKodu ?? "");
        sv("fAks", dto.aksTahrigiTanimi ?? "");
        sv("fEks", dto.eksDonanim ?? "");
    }

    function temizForm() {
        if (LEGACY_VW && legacyVwShell()) {
            temizFormLegacyVw();
            return;
        }

        editingId = null;
        const btnKaydet = document.getElementById("btnKaydet");
        if (btnKaydet) btnKaydet.innerHTML = '<i class="bi bi-save me-1"></i>Kaydet';

        sv("fBaslik", "");
        sv("fDegerliAciklama", "");
        for (let i = 1; i <= 9; i++) sv("fSaseKod" + i, "");
        clearSecilenStoklar();

        sv("fModel", "");
        sv("fModelYili", "");
        sv("fUretBas", "");
        sv("fUretBit", "");
        sv("fSatisTipi", "");
        sv("fMotorKodu", "");
        sv("fSanzimanKodu", "");
        sv("fAks", "");
        sv("fEks", "");
        sv("fPsaUrunGam", "");
        sv("fPsaOpr", "");
        sv("fPsaOprBas", "");
        sv("fPsaOprBit", "");
        sv("fPsaAktarma", "");
        sv("fPsaGarantBas", "");
        sv("fPsaGarantBit", "");
    }

    function buildListeQueryParams() {
        const p = new URLSearchParams();
        p.set("page", String(pagingMeta.page));
        p.set("size", String(pagingMeta.size));
        document.querySelectorAll(".filter-inp[data-f], .filter-select[data-f]").forEach(el => {
            /** @type {HTMLInputElement|HTMLSelectElement} */
            const inp = /** @type {HTMLInputElement|HTMLSelectElement} */ (el);
            const key = inp.getAttribute("data-f");
            const v = inp.value.trim();
            if (key && v !== "") p.append(key, v);
        });
        return p;
    }

    async function listeYukle() {
        const body = /** @type {HTMLElement} */ (document.getElementById("tblSaseBody"));
        if (!body) return;
        body.innerHTML = '<tr><td colspan="12" class="text-center py-4 text-muted">Yükleniyor…</td></tr>';

        try {
            const data = /** @type {any} */ (await apiJson("GET", API_LIST + "?" + buildListeQueryParams().toString(), null));
            pagingMeta.totalCount = Number(data.totalCount || 0);
            pagingMeta.page = Number(data.page || 0);
            pagingMeta.size = Number(data.pageSize || pagingMeta.size);
            pagingMeta.totalPages = Number(data.totalPages || 1);
            lastRows = data.data || [];
            redrawTable();
            sayfaDurumunuGuncelle();
        } catch (e) {
            const msg = e instanceof Error ? e.message : "Hata";
            body.innerHTML = '<tr><td colspan="12" class="text-center py-5 text-danger">' + escapeHtml(msg) + "</td></tr>";
            toast(msg, false);
        }
    }

    function redrawTable() {
        const body = /** @type {HTMLElement} */ (document.getElementById("tblSaseBody"));
        if (!body) return;
        const loc = gv("filtKullanicilokal").toLowerCase();

        const rows = lastRows.filter(/** @param {any} r */ r => {
            if (!loc) return true;
            const bag = `${r.kullaniciAdi ?? ""} ${r.adSoyad ?? ""}`.toLowerCase();
            return bag.includes(loc);
        });

        if (!rows.length) {
            body.innerHTML = '<tr><td colspan="12" class="text-center py-5 text-muted">Kayıt yok.</td></tr>';
            return;
        }

        const html = [];
        for (const r of rows) {
            const stk = Array.isArray(r.stokDetaylari)
                ? r.stokDetaylari.map(/** @param {any} x */ x => escapeHtml(String(x.stokKodu ?? ""))).join(", ")
                : "–";
            const exec = !!(r.executed === true || r.executed === "true");
            html.push(`
<tr class="${exec ? "exec-row-done" : "exec-row-pending"}"
    style="background-color:${exec ? "#e8f5e9" : "#fffde7"};"
    data-id="${r.id}">
  <td class="fw-semibold small">${escapeHtml(String(r.baslik || ""))}</td>
  <td class="small">
    ${escapeHtml(String(r.kullaniciAdi || "–"))}
    <div class="text-muted">${escapeHtml(String(r.adSoyad || ""))}</div>
  </td>
  <td class="small">${stk}</td>
  ${[1, 2, 3, 4, 5, 6].map(i => {
                const kk = r["saseKod" + i];
                return `<td class="small text-muted text-truncate" style="max-width:120px">${escapeHtml(String((kk ?? "").substring(0, 80)))}</td>`;
            }).join("")}
  <td class="text-center small">${exec ? "✓" : "…"}</td>
  <td class="text-nowrap">
    <button type="button" class="btn btn-outline-primary btn-sm btn-edit-row px-2 py-1" title="Düzenle"><i class="bi bi-pencil-square"></i></button>
    <button type="button" class="btn btn-outline-danger btn-sm btn-del-row px-2 py-1" title="Sil"><i class="bi bi-trash"></i></button>
    <button type="button" class="btn btn-outline-warning btn-sm btn-run-row px-2 py-1" title="Çalıştır"><i class="bi bi-play-fill"></i></button>
  </td>
</tr>`);
        }

        body.innerHTML = html.join("");
        body.querySelectorAll(".btn-del-row").forEach(btn => btn.addEventListener("click", onSil));
        body.querySelectorAll(".btn-edit-row").forEach(btn => btn.addEventListener("click", onDuzenle));
        body.querySelectorAll(".btn-run-row").forEach(btn => btn.addEventListener("click", onSatirCalistir));
    }

    function sayfaDurumunuGuncelle() {
        const lbl = document.getElementById("lblSayfaNo");
        if (lbl) lbl.textContent = `${pagingMeta.page + 1} / ${Math.max(pagingMeta.totalPages, 1)}`;
        const lbl2 = document.getElementById("lblSayfaBilgi");
        if (lbl2) lbl2.textContent = pagingMeta.totalCount + " kayıt";
    }

    async function kaydetKayit() {
        try {
            /** @type {Record<string,null|string|string[]>} */
            const dto = collectDto();
            let out;
            if (editingId) {
                out = /** @type {any} */ (await apiJson("PUT", API_LIST + "/" + editingId, dto));
                toast("Güncellendi");
            } else {
                out = /** @type {any} */ (await apiJson("POST", API_LIST, dto));
                toast("Kayıt oluşturuldu");
            }
            editingId = out?.id ?? editingId;
            await listeYukle();
            document.getElementById("form-anchor")?.scrollIntoView({ behavior: "smooth", block: "start" });
        } catch (e) {
            toast(e instanceof Error ? e.message : "Hata", false);
        }
    }

    async function tumBekleyenleriCalistir(confirmOnce = true) {
        if (confirmOnce && !confirm("Bekleyen tüm kayıtlar işaretlenecek. Devam?")) return;
        try {
            await apiJson("POST", API_LIST + "/calistir-hepsini", null);
            toast("Tamamlandı");
            await listeYukle();
        } catch (e) {
            toast(e instanceof Error ? e.message : "Hata", false);
        }
    }

    /** @param {Event} ev */
    async function onSatirCalistir(ev) {
        const btn = /** @type {HTMLElement} */ (/** @type {any} */ (ev.currentTarget));
        const tr = btn.closest("tr");
        const id = tr?.dataset.id;
        if (!id) return;
        try {
            await apiJson("POST", API_LIST + "/" + id + "/calistir", null);
            toast("Çalıştırıldı");
            await listeYukle();
        } catch (e) {
            toast(e instanceof Error ? e.message : "Hata", false);
        }
    }

    /** @param {Event} ev */
    async function onSil(ev) {
        const btn = /** @type {HTMLElement} */ (/** @type {any} */ (ev.currentTarget));
        const tr = btn.closest("tr");
        const id = tr?.dataset.id;
        if (!id || !confirm("Bu kaydı silmek istediğinizden emin misiniz?")) return;
        try {
            await apiJson("DELETE", API_LIST + "/" + id, null);
            toast("Silindi");
            if (editingId === id) temizForm();
            await listeYukle();
        } catch (e) {
            toast(e instanceof Error ? e.message : "Hata", false);
        }
    }

    /** @param {string|undefined|null} rowId */
    async function kayitOlaniFormdaAc(rowId) {
        const id = rowId == null ? "" : String(rowId).trim();
        if (!id) return;
        try {
            const dto = /** @type {any} */ (await apiJson("GET", API_LIST + "/" + id, null));
            formuDtoIleDoldur(dto);
            toast("Kayıt forma yüklendi");
        } catch (e) {
            toast(e instanceof Error ? e.message : "Hata", false);
        }
    }

    /** @param {Event} ev */
    async function onDuzenle(ev) {
        const btn = /** @type {HTMLElement} */ (/** @type {any} */ (ev.currentTarget));
        const tr = btn.closest("tr");
        const id = tr?.dataset.id;
        if (!id) return;
        await kayitOlaniFormdaAc(id);
        document.getElementById("form-anchor")?.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    async function formdaCalistir() {
        if (!editingId) {
            toast("Önce satır seçip düzenleyin ya da kaydedilmiş bir kayıt yükleyin.", false);
            return;
        }
        try {
            await apiJson("POST", API_LIST + "/" + editingId + "/calistir", null);
            toast("Çalıştırıldı");
            await listeYukle();
        } catch (e) {
            toast(e instanceof Error ? e.message : "Hata", false);
        }
    }

    async function stokAraGoster() {
        const tbody = /** @type {HTMLElement} */ (document.getElementById("stokBulBody"));
        const q = gv("inpStokAra");
        if (!tbody) return;
        tbody.innerHTML = '<tr><td colspan="3" class="text-muted">Aranıyor…</td></tr>';
        try {
            /** @type {any[]} */
            const rows = await apiJson(
                "GET",
                API_LIST + "/stok-ara?q=" + encodeURIComponent(q) + "&limit=80",
                null
            );

            tbody.innerHTML = rows.map(/** @param {any} st */ st => `
<tr>
  <td><code>${escapeHtml(String(st.stokKodu ?? ""))}</code></td>
  <td>${escapeHtml(String(st.stokAdi ?? ""))}</td>
  <td><button type="button" class="btn btn-sm btn-outline-primary btn-stok-select" data-id="${escapeHtml(String(st.id))}" data-code="${escapeHtml(String(st.stokKodu ?? ""))}">Seç</button></td>
</tr>`).join("") || '<tr><td colspan="3" class="text-muted">Sonuç yok.</td></tr>';

            tbody.querySelectorAll(".btn-stok-select").forEach(b => {
                b.addEventListener("click", () => {
                    const bid = /** @type {HTMLElement} */ (b).getAttribute("data-id") || "";
                    const code = /** @type {HTMLElement} */ (b).getAttribute("data-code") || "";
                    attachStok({ id: bid, stokKodu: code });
                    toast("Stok eklendi");
                });
            });
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-danger">Arama yapılamadı.</td></tr>';
            toast(e instanceof Error ? e.message : "Hata", false);
        }
    }

    function vwFormJsonSnapshot() {
        /** @type {Record<string, unknown>} */
        const o = {
            formulId: gv("vwRoId"),
            baslik: gv("fBaslik"),
            gruplar: {
                g1: gv("vwG1"),
                g2: gv("vwG2"),
                g3: gv("vwG3"),
                g4: { a: gv("vwG4a"), b: gv("vwG4b"), k: gv("vwK_g4") },
                g5: gv("vwG5"),
                g6: { a: gv("vwG6a"), b: gv("vwG6b"), k: gv("vwK_g6") },
            },
            arac: {
                model: gv("fModel"),
                uretBas: gv("fUretBas"),
                uretBit: gv("fUretBit"),
                modelYili: gv("fModelYili"),
                satisTipi: gv("fSatisTipi"),
                motor: gv("fMotorKodu"),
                sanziman: gv("fSanzimanKodu"),
                aks: gv("fAks"),
                eksDonanim: gv("fEks"),
            },
            kriter_secimleri: kritleriAlVwUi(),
            pins: [...Array.from({ length: 9 })].map((_, i) => gv("vwPin" + (i + 1))),
            olmayanSase: gv("vwBlacklist"),
            degerliStok: gv("vwDegerliStokTek"),
            stokKoduListesiRaw: gv("fStokKodListe"),
        };
        return o;
    }

    async function vwKopyalaTikla() {
        try {
            if (!editingId) {
                toast("Kopyalamak için önce kayıtlı bir formül açın veya oluşturup kaydedin.", false);
                return;
            }
            await apiJson("POST", API_LIST + "/" + encodeURIComponent(editingId) + "/kopyala", null);
            toast("Formül kopyalandı. Liste açılıyor.", true);
            window.location.assign("/sase/vw");
        } catch (e) {
            toast(e instanceof Error ? e.message : "Kopyalanamadı.", false);
        }
    }

    async function vwYeniKayitKaydetTikla() {
        try {
            if (vwTuruncularKontrol()) {
                toast("Formül başlığı zorunludur.", false);
                return;
            }
            virgulleEnFazla5VeEoOlmaz("3. grup", gv("vwG3"), 5);
            virgulleEnFazla5VeEoOlmaz("5. grup", gv("vwG5"), 5);
            grup6IkiliSayilar();

            /** @type {any} */
            const dto = await collectDtoLegacyVw();
            let out;
            if (editingId) {
                out = await apiJson("PUT", API_LIST + "/" + editingId, dto);
            } else {
                out = await apiJson("POST", API_LIST, dto);
            }
            editingId = out?.id ?? editingId;
            alert("Kayıt başarıyla kaydedildi.");
            toast(editingId ? "Güncellendi." : "Oluşturuldu.", true);
            formuDtoIleDoldurVw(out);
            await listeYukle();
        } catch (e) {
            const msg = e instanceof Error ? e.message : String(e);
            toast(msg, false);
        }
    }

    async function vwCalistirmamisMockVeApi() {
        alert("Çalıştırılmamış kayıtlar çalıştırılıyor...");
        await tumBekleyenleriCalistir(false);
    }

    /** Prompt: 3/5. grup virgül — max 5 parça; é ile virgül bir arada olmasın. */
    function vwBindGrupVirgulBlur() {
        const latinE = "\u00e9";
        ["vwG3", "vwG5"].forEach(id => {
            const el = /** @type {HTMLInputElement|null} */ (document.getElementById(id));
            if (!el) return;
            el.addEventListener("blur", () => {
                const v = String(el.value || "");
                const pieces = v.split(",").filter(x => x.trim() !== "");
                if (pieces.length > 5) {
                    el.style.border = "2px solid #c53030";
                    alert("En fazla 5 farklı değer girebilirsiniz.");
                    return;
                }
                if (v.includes(",") && v.includes(latinE)) {
                    el.style.border = "2px solid #c53030";
                    alert("Virgüllü girişlerde é karakterine izin verilmez.");
                    return;
                }
                el.style.border = "";
            });
        });
    }

    /** Prompt: 6. grup kısa kutular — boş veya 0..999999 tam sayı */
    function vwBindGrup6NumericBlur() {
        ["vwG6a", "vwG6b"].forEach(id => {
            const el = /** @type {HTMLInputElement|null} */ (document.getElementById(id));
            if (!el) return;
            el.addEventListener("blur", () => {
                const t = String(el.value || "").trim();
                if (!t) {
                    el.style.border = "";
                    return;
                }
                if (!/^\d+$/.test(t)) {
                    el.style.border = "2px solid #c53030";
                    alert("0 ile 999.999 arasında sayısal değer giriniz.");
                    return;
                }
                const n = Number(t);
                if (n < 0 || n > 999_999) {
                    el.style.border = "2px solid #c53030";
                    alert("0 ile 999.999 arasında sayısal değer giriniz.");
                    return;
                }
                el.style.border = "";
            });
        });
    }

    function init_legacy_vw_bindings() {
        document.getElementById("btnVwGer")?.addEventListener("click", () => {
            if (window.history.length > 1) window.history.back();
            else window.location.href = "/sase/vw";
        });
        document.getElementById("btnVwCopy")?.addEventListener("click", () => vwKopyalaTikla());
        document.getElementById("btnVwKaydet")?.addEventListener("click", () => vwYeniKayitKaydetTikla());
        document.getElementById("btnVwDegAcikl")?.addEventListener("click", () => {
            const t = prompt("Değerli açıklama için stok kod(larını) girin:");
            if (t == null || !t.trim()) return;
            sv("vwDegerliStokTek", gv("vwDegerliStokTek") ? gv("vwDegerliStokTek") + " · " + t.trim() : t.trim());
            toast("Değerli açıklama alanı güncellendi");
        });
        document.getElementById("btnVwCalistirHepsi")?.addEventListener("click", () => {
            vwCalistirmamisMockVeApi().catch(e => toast(e instanceof Error ? e.message : "Hata", false));
        });

        /** Satır çift tıklama — düzenle */
        document.getElementById("tblSase")?.addEventListener("dblclick", /** @param {MouseEvent} ev */ ev => {
            if (!(ev.target instanceof Element)) return;
            if (ev.target.closest("button")) return;
            const tr = ev.target.closest("tr[data-id]");
            const rid = /** @type {HTMLElement|null} */ (tr)?.dataset?.id;
            if (!rid) return;
            kayitOlaniFormdaAc(rid).then(() => {
                legacyVwShell()?.scrollIntoView({ behavior: "smooth", block: "start" });
            });
        });

        vwBindGrupVirgulBlur();
        vwBindGrup6NumericBlur();
    }

    function init() {
        const selBoy = /** @type {HTMLSelectElement} */ (document.getElementById("selSayfaBoyutu"));
        if (selBoy) {
            selBoy.value = String(pagingMeta.size);
            selBoy.addEventListener("change", () => {
                pagingMeta.size = parseInt(selBoy.value, 10) || 25;
                pagingMeta.page = 0;
                listeYukle();
            });
        }

        document.getElementById("btnPrevSayfa")?.addEventListener("click", () => {
            if (pagingMeta.page > 0) {
                pagingMeta.page--;
                listeYukle();
            }
        });
        document.getElementById("btnNextSayfa")?.addEventListener("click", () => {
            if (pagingMeta.page + 1 < pagingMeta.totalPages) {
                pagingMeta.page++;
                listeYukle();
            }
        });
        document.getElementById("filtKullanicilokal")?.addEventListener("input", debounce(() => redrawTable(), 220));

        const debouncedReload = debounce(() => {
            pagingMeta.page = 0;
            listeYukle();
        }, 340);
        document.querySelectorAll(".filter-inp[data-f], .filter-select[data-f]").forEach(el => {
            el.addEventListener("input", debouncedReload);
            el.addEventListener("change", debouncedReload);
        });

        const legacy = LEGACY_VW && !!legacyVwShell();

        if (legacy) {
            init_legacy_vw_bindings();
            console.info("VW masaüstü şase formu hazır");
        } else {
            document.getElementById("btnKaydet")?.addEventListener("click", kaydetKayit);
            document.getElementById("btnCalistirHepsi")?.addEventListener("click", () =>
                tumBekleyenleriCalistir(true)
            );
            document.getElementById("btnCalistir")?.addEventListener("click", formdaCalistir);
            document.getElementById("btnFormTemiz")?.addEventListener("click", () => {
                temizForm();
                toast("Form sıfırlandı");
            });
            document.getElementById("btnStokListeTemiz")?.addEventListener("click", () => {
                clearSecilenStoklar();
                toast("Stok listesi temizlendi");
            });
            document.getElementById("btnStokSearchGo")?.addEventListener("click", stokAraGoster);
        }

        listeYukle();
        console.info("Şase formül UI:", MARKA, legacy ? "legacy VW" : "standart");
    }

    /**
     * /sase/vw → Seç: /sase/vw/formuls?vfUuid=... (vw-formul-loader.js).
     * @param {string} uuid
     * @returns {Promise<void>}
     */
    window.__vwFormulLoadKayit = async function (uuid) {
        const u = (uuid ?? "").trim();
        if (!u) return;
        try {
            const dto = await apiJson("GET", API_LIST + "/" + encodeURIComponent(u), null);
            formuDtoIleDoldur(dto);
        } catch (e) {
            const m = e instanceof Error ? e.message : String(e);
            alert(m);
            throw e;
        }
    };

    document.addEventListener("DOMContentLoaded", init);
})();

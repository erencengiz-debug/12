/**
 * /sase manuel araç hub — cascade filtreler ve şase arama.
 */
(function () {
    const root = document.querySelector(".sase-manuel-page");
    if (!root) return;

    const apiBase = (typeof window.__SASE_MANUEL_API_BASE === "string" && window.__SASE_MANUEL_API_BASE) || "/api/sase";
    const ctxStokHref = typeof window.__SASE_MANUEL_STOK_PAGE === "string" ? window.__SASE_MANUEL_STOK_PAGE : "/stok";

    const selSlug = /** @type {HTMLSelectElement|null} */ (document.getElementById("selManuelModel"));
    const selAlt = /** @type {HTMLSelectElement|null} */ (document.getElementById("selManuelAltModel"));
    const selYil = /** @type {HTMLSelectElement|null} */ (document.getElementById("selManuelYil"));
    const selKat1 = /** @type {HTMLSelectElement|null} */ (document.getElementById("selManuelKat1"));
    const selKat2 = /** @type {HTMLSelectElement|null} */ (document.getElementById("selManuelKat2"));
    const selAck1 = /** @type {HTMLSelectElement|null} */ (document.getElementById("selManuelAck1"));
    const selAck2 = /** @type {HTMLSelectElement|null} */ (document.getElementById("selManuelAck2"));
    const brands = Array.from(document.querySelectorAll(".sase-manuel-brand[data-slug]"));
    const vinInput = document.getElementById("inpManuelVin");
    const btnDots = document.getElementById("btnManuelDots");
    const btnGetir = document.getElementById("btnManuelGetir");

    /** @type {string|null} */
    let activeSlug = null;
    /** @type {ReturnType<typeof setTimeout>} */
    let _debounce = 0;

    function slug() {
        return activeSlug;
    }

    function debounceReload() {
        clearTimeout(_debounce);
        _debounce = setTimeout(() => {
            reloadOptions().catch((e) => alert(e.message || String(e)));
        }, 180);
    }

    /** @type {HTMLElement|null} */
    function toastErr(msg) {
        alert(msg);
    }

    async function apiJsonGet(url) {
        const res = await fetch(url, { credentials: "include" });
        const text = await res.text();
        let j = /** @type {{success?:boolean,message?:string,data?:unknown}} */ ({});
        if (text && text.trim()) {
            try {
                j = JSON.parse(text);
            } catch {
                throw new Error("Sunucu yanıtı okunamadı.");
            }
        }
        if (!j.success) throw new Error(j.message || "İstek başarısız");
        return j.data;
    }

    function setBrandSelected(el) {
        brands.forEach((b) => b.classList.toggle("is-selected", b === el));
    }

    /** Wikimedia CDN vs.; URL ölürse kısa marka yazısı göster */
    function wireBrandLogoFallbacks() {
        root.querySelectorAll(".sase-manuel-brand-logo").forEach((node) => {
            const img = /** @type {HTMLImageElement} */ (node);
            if (!(img instanceof HTMLImageElement)) return;
            img.addEventListener(
                "error",
                () => {
                    const altImg = img.getAttribute("data-fallback-img-src");
                    if (altImg && img.dataset.logoFallbackAttempt !== "1") {
                        img.dataset.logoFallbackAttempt = "1";
                        img.removeAttribute("data-fallback-img-src");
                        img.src = altImg;
                        return;
                    }
                    img.style.display = "none";
                    const box = img.closest(".sase-manuel-brand");
                    if (!(box instanceof HTMLElement) || box.querySelector(".sase-manuel-brand-fallback")) return;
                    const span = document.createElement("span");
                    span.className = "sase-manuel-brand-fallback";
                    const label = img.getAttribute("data-fallback") || img.alt || "";
                    span.textContent = label.trim() || "?";
                    box.appendChild(span);
                },
                { once: false }
            );
        });
    }
    wireBrandLogoFallbacks();

    function enableFilters(on) {
        [selSlug, selAlt, selYil, selKat1, selKat2, selAck1, selAck2].forEach((s) => {
            if (!s) return;
            s.disabled = !on;
            if (!on) {
                resetSelect(s);
            }
        });
    }

    function resetSelect(sel) {
        const ph = sel.getAttribute("data-ph") || "";
        sel.innerHTML = "";
        const o = document.createElement("option");
        o.value = "";
        o.textContent = ph;
        sel.appendChild(o);
        sel.selectedIndex = 0;
    }

    function refill(sel, opts, preserveValue) {
        if (!sel) return;
        const prev = preserveValue ? sel.value : "";
        resetSelect(sel);
        const list = Array.isArray(opts) ? opts : [];
        for (const t of list) {
            if (t == null || String(t).trim() === "") continue;
            const o = document.createElement("option");
            const v = String(t).trim();
            o.value = v;
            o.textContent = v;
            sel.appendChild(o);
        }
        if (prev && [...sel.options].some((op) => op.value === prev)) {
            sel.value = prev;
        }
    }

    async function reloadOptions() {
        const s = slug();
        if (!s) return;
        const params = new URLSearchParams({ markaSlug: s });
        const m = selSlug && selSlug.value ? selSlug.value : "";
        const a = selAlt && selAlt.value ? selAlt.value : "";
        const k1 = selKat1 && selKat1.value ? selKat1.value : "";
        const k2 = selKat2 && selKat2.value ? selKat2.value : "";
        const a1 = selAck1 && selAck1.value ? selAck1.value : "";

        if (m) params.set("model", m);
        if (a) params.set("altModel", a);
        if (k1) params.set("kat1", k1);
        if (k2) params.set("kat2", k2);
        if (a1) params.set("aciklama1", a1);

        const data = /** @type {any} */ (await apiJsonGet(apiBase + "/manuel-arac/filtreler?" + params.toString()));

        refill(selSlug, Array.isArray(data.modeller) ? data.modeller : [], true);
        refill(selAlt, Array.isArray(data.altModeller) ? data.altModeller : [], true);
        refill(selYil, Array.isArray(data.yillar) ? data.yillar : [], true);
        refill(selKat1, Array.isArray(data.kategori1Liste) ? data.kategori1Liste : [], true);
        refill(selKat2, Array.isArray(data.kategori2Liste) ? data.kategori2Liste : [], true);
        refill(selAck1, Array.isArray(data.aciklama1Liste) ? data.aciklama1Liste : [], true);
        refill(selAck2, Array.isArray(data.aciklama2Liste) ? data.aciklama2Liste : [], true);
    }

    brands.forEach((box) => {
        box.addEventListener("click", () => {
            const s = box.getAttribute("data-slug");
            activeSlug = s && s.trim() ? s.trim() : null;
            setBrandSelected(box);
            if (!activeSlug) {
                enableFilters(false);
                return;
            }
            enableFilters(true);
            selAlt && resetSelect(selAlt);
            selYil && resetSelect(selYil);
            selKat1 && resetSelect(selKat1);
            selKat2 && resetSelect(selKat2);
            selAck1 && resetSelect(selAck1);
            selAck2 && resetSelect(selAck2);
            reloadOptions().catch((e) => toastErr(e.message || String(e)));
        });

        const href = box.getAttribute("data-formul-href");
        if (href) {
            box.addEventListener("dblclick", (ev) => {
                ev.preventDefault();
                window.location.href = href;
            });
        }
    });

    [selSlug, selAlt, selKat1, selKat2, selAck1].forEach((sel) => {
        sel?.addEventListener("change", () => {
            if (!slug()) return;
            if (sel === selSlug) {
                selAlt && resetSelect(selAlt);
                selKat1 && resetSelect(selKat1);
                selKat2 && resetSelect(selKat2);
                selAck1 && resetSelect(selAck1);
                selAck2 && resetSelect(selAck2);
            }
            if (sel === selAlt) {
                selKat1 && resetSelect(selKat1);
                selKat2 && resetSelect(selKat2);
                selAck1 && resetSelect(selAck1);
                selAck2 && resetSelect(selAck2);
            }
            if (sel === selKat1) {
                selKat2 && resetSelect(selKat2);
                selAck1 && resetSelect(selAck1);
                selAck2 && resetSelect(selAck2);
            }
            if (sel === selKat2) {
                selAck1 && resetSelect(selAck1);
                selAck2 && resetSelect(selAck2);
            }
            if (sel === selAck1) {
                selAck2 && resetSelect(selAck2);
            }
            debounceReload();
        });
    });

    enableFilters(false);

    btnDots?.addEventListener("click", () => {
        const parts = [selSlug, selAlt, selYil, selKat1, selKat2, selAck1, selAck2]
            .map((s) => (s && /** @type {HTMLSelectElement} */ (s).value ? /** @type {HTMLSelectElement} */ (s).value.trim() : ""))
            .filter(Boolean);
        if (!slug()) {
            toastErr("Önce bir marka kutusu seçin.");
            return;
        }
        if (parts.length === 0) {
            toastErr("Stokta arama için önce filtrelerden en az bir değer seçin.");
            return;
        }
        const q = parts.join(" ");
        const sep = ctxStokHref.includes("?") ? "&" : "?";
        window.location.href = ctxStokHref + sep + "q=" + encodeURIComponent(q);
    });

    btnGetir?.addEventListener("click", async () => {
        const radios = /** @type {NodeListOf<HTMLInputElement>} */ (
            root.querySelectorAll('input[type="radio"][name="manuelAramaMod"]')
        );
        const sel = [...radios].find((r) => r.checked);
        const mode = sel ? sel.value : "sase";
        if (mode !== "sase") {
            toastErr(
                mode === "plaka"
                    ? "Plaka ile arama henüz devrede değil; yalnızca Şase No ile getir kullanılabilir."
                    : "İsim ile arama henüz devrede değil; yalnızca Şase No ile getir kullanılabilir."
            );
            return;
        }
        const q = vinInput instanceof HTMLInputElement ? vinInput.value.trim() : "";
        if (!q) {
            toastErr("Şase numarası girin.");
            vinInput instanceof HTMLInputElement && vinInput.focus();
            return;
        }
        try {
            const data = /** @type {any[]} */ (
                await apiJsonGet(apiBase + "/arama/kayit-metni?q=" + encodeURIComponent(q))
            );
            if (!Array.isArray(data) || data.length === 0) {
                toastErr("Şase için eşleşen kayıt bulunamadı.");
                return;
            }
            const detailBase =
                typeof window.__SASE_MANUEL_SASE_DETAIL_BASE === "string"
                    ? window.__SASE_MANUEL_SASE_DETAIL_BASE.replace(/\/+$/, "")
                    : "/sase";
            if (data.length === 1 && data[0].id) {
                window.location.href = detailBase + "/" + data[0].id;
                return;
            }
            const lines = data
                .slice(0, 15)
                .map(
                    (row, i) =>
                        `${i + 1}) ${String(row.baslik ?? "").trim()}  [${String(row.id)}]`
                )
                .join("\n");
            const pick =
                prompt(
                    `Birden çok kayıt bulundu. Gitmek için satır numarası yazın:\n\n${lines}\n\n(Numara 1–${Math.min(data.length, 15)}, iptal için boş)`
                );
            if (!pick) return;
            const n = parseInt(pick, 10);
            if (Number.isNaN(n) || n < 1 || n > Math.min(data.length, 15)) {
                toastErr("Geçersiz seçim.");
                return;
            }
            const chosen = data[n - 1];
            const detailBase =
                typeof window.__SASE_MANUEL_SASE_DETAIL_BASE === "string"
                    ? window.__SASE_MANUEL_SASE_DETAIL_BASE.replace(/\/+$/, "")
                    : "/sase";
            window.location.href = detailBase + "/" + chosen.id;
        } catch (e) {
            toastErr(e instanceof Error ? e.message : String(e));
        }
    });

    vinInput?.addEventListener("keydown", (ev) => {
        if (ev.key === "Enter") {
            ev.preventDefault();
            btnGetir instanceof HTMLElement && btnGetir.click();
        }
    });
})();

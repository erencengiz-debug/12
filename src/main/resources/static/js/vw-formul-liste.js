/**
 * /sase/vw tablo istemci tarafı filtreleri ve Sil / Satır Temizle / Genel filtre sıfır.
 */
(function () {
    const tbl = document.getElementById("tblVwFormuls");
    if (!tbl || !tbl.tBodies.length) return;

    const tbody = tbl.tBodies[0];
    const filterRow = tbl.querySelector("tr.vw-filter-row");
    if (!filterRow || !tbody) return;

    /** Hangi {@code data-filter-key} hangi {@code td} class'ına denk geliyor */
    const KEY_CLASS = /** @type {Record<string,string>} */ ({
        id: "tcol-id",
        ekleyen: "tcol-ekleyen",
        baslik: "tcol-baslik",
        stokliste: "tcol-stokliste",
        sk1: "tcol-sk1",
        sk2: "tcol-sk2",
        sk3: "tcol-sk3",
        sk4: "tcol-sk4",
        sk5: "tcol-sk5",
        sk6: "tcol-sk6",
        sk7: "tcol-sk7",
        sk8: "tcol-sk8",
        sk9: "tcol-sk9",
        saseno: "tcol-saseno",
        model: "tcol-model",
        uretbas: "tcol-uretbas",
        uretbit: "tcol-uretbit",
        modelYili: "tcol-modelYili",
        satisTipi: "tcol-satisTipi",
        motor: "tcol-motor",
        sanziman: "tcol-sanziman",
        aks: "tcol-aks",
        donanim: "tcol-donanim",
        execdate: "tcol-execdate",
        eslenik: "tcol-eslenik",
        degerli: "tcol-degerli",
    });

    /** @returns {HTMLElement[]} */
    function filterControls() {
        return Array.from(filterRow.querySelectorAll("[data-filter-key]"));
    }

    /** @param {HTMLElement} row @param {string} cls */
    function cellText(row, cls) {
        const el = row.querySelector("." + cls);
        return el ? el.textContent.trim() : "";
    }

    /** @param {HTMLElement} row */
    function executedMatches(row, want) {
        if (!want) return true;
        const v = (row.dataset.executed || "").toLowerCase();
        if (want === "yes") return v === "true";
        if (want === "no") return v === "false" || v === "";
        return true;
    }

    /** @param {HTMLElement} row */
    function matches(row) {
        const execCtl = /** @type {HTMLSelectElement|null} */(filterRow.querySelector('[data-filter-key="executed"]'));
        const execWant = execCtl && execCtl.value ? execCtl.value : "";
        if (!executedMatches(row, execWant)) return false;

        const ekSel = /** @type {HTMLSelectElement|null} */(filterRow.querySelector('[data-filter-key="ekleyenSel"]'));
        const cellEk = cellText(row, KEY_CLASS["ekleyen"]);
        if (ekSel && ekSel.value.trim() && cellEk !== ekSel.value.trim()) return false;

        const ekFree = /** @type {HTMLInputElement|null} */(filterRow.querySelector('input[data-filter-key="ekleyen"]'));
        if (ekFree && ekFree.value.trim()) {
            if (!cellEk.toLowerCase().includes(ekFree.value.trim().toLowerCase())) return false;
        }

        for (const el of filterControls()) {
            const key = el.getAttribute("data-filter-key");
            if (!key || key === "executed" || key === "ekleyenSel" || key === "ekleyen") continue;
            const q = ("value" in el ? /** @type {HTMLInputElement|HTMLSelectElement}*/ (el).value : "").trim().toLowerCase();
            if (!q) continue;

            if (key === "id") {
                const rid = (row.getAttribute("data-row-id") || "").toLowerCase();
                if (!rid.includes(q)) return false;
                continue;
            }

            const cls = KEY_CLASS[key];
            if (!cls) continue;
            const txt = cellText(row, cls).toLowerCase();
            if (!txt.includes(q)) return false;
        }
        return true;
    }

    function applyFilters() {
        for (const row of tbody.querySelectorAll("tr")) {
            if (!row.getAttribute("data-row-id")) {
                row.classList.remove("d-none");
                continue;
            }
            row.classList.toggle("d-none", !matches(row));
        }
    }

    /** @returns {Promise<void>} */
    function doApi(method, url) {
        return fetch(url, { method: method, credentials: "include" }).then(async (res) => {
            const text = await res.text();
            /** @type {{success?:boolean,message?:string}} */
            let j = {};
            if (text && text.trim()) {
                try {
                    j = JSON.parse(text);
                } catch {
                    throw new Error("Geçersiz sunucu yanıtı (HTTP " + res.status + ")");
                }
            }
            if (!res.ok || typeof j.success !== "boolean") {
                throw new Error(j.message || "İstek başarısız (HTTP " + res.status + ")");
            }
            if (!j.success) throw new Error(j.message || "İşlem başarısız");
        });
    }

    function showErr(/** @type {unknown} */ err) {
        const m = err instanceof Error ? err.message : String(err);
        alert(m);
    }

    for (const el of filterControls()) {
        el.addEventListener("input", applyFilters);
        el.addEventListener("change", applyFilters);
    }

    const btnClr = document.getElementById("btnVwListeFiltreTemizle");
    if (btnClr) {
        btnClr.addEventListener("click", function (ev) {
            ev.preventDefault();
            for (const el of filterControls()) {
                if ("value" in el) /** @type {HTMLInputElement|HTMLSelectElement} */ (el).value = "";
            }
            applyFilters();
        });
    }

    tbody.addEventListener("click", function (ev) {
        const t = ev.target;
        if (!(t instanceof Element)) return;
        const btnSil = t.closest(".vw-btn-sil");
        if (btnSil) {
            const id = btnSil.getAttribute("data-id");
            if (!id || !confirm("Bu kaydı silmek istediğinize emin misiniz?")) return;
            const apiBase = window.__API_SASE_BASE || "/api/sase";
            doApi("DELETE", apiBase + "/" + encodeURIComponent(id))
                .then(() => location.reload())
                .catch(showErr);
            return;
        }
        const btnTemizle = t.closest(".vw-btn-satir-temizle");
        if (btnTemizle) {
            const id = btnTemizle.getAttribute("data-id");
            if (!id || !confirm("Bu satır veritabanında temizlenecek (alanlar sıfırlanacak). Devam?")) return;
            const apiBase = window.__API_SASE_BASE || "/api/sase";
            doApi("POST", apiBase + "/" + encodeURIComponent(id) + "/alan-temizle")
                .then(() => location.reload())
                .catch(showErr);
        }
    });

    applyFilters();
})();

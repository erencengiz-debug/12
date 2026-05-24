/**
 * VW formül sayfası: URL ?vfUuid= ile /api/sase/{uuid} kaydını yükler (legacy VW formu, sase-eslestirme.js).
 */
(function () {
    if (!document.getElementById("legacyVwShell")) return;

    const p = new URLSearchParams(window.location.search);
    const vfUuidRaw = p.get("vfUuid") || p.get("vfuuid") || "";
    const vfUuid = vfUuidRaw.trim();
    const reUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    if (!vfUuid || !reUuid.test(vfUuid)) return;

    async function load() {
        const loadFn = window.__vwFormulLoadKayit;
        if (typeof loadFn !== "function") {
            alert("Form yükleyici hazır değil (sase-eslestirme.js).");
            return;
        }
        await loadFn(vfUuid);
        history.replaceState(null, "", window.location.pathname + window.location.hash);
    }

    load().catch(e => alert(e.message || String(e)));
})();

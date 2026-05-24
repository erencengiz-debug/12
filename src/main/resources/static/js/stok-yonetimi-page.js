(function () {
    var KEYS = ['foto', 'alis', 'cikis', 'orj', 'model', 'marka', 'sad', 'asad', 'ksa', 'aksi', 'fy1', 'fy2', 'fy3', 'fy10', 'rek', 'bak', 'onl', 'kat1', 'kat2', 'dmr', 'dmt', 'stat', 'crt'];
    var ONLY_DD = { rek: true, bak: true, onl: true, kat1: true, kat2: true, stat: true };

    var mem = [];
    var hit = [];
    var txtFil = {};
    var popFil = {};
    var pg = 0;
    var PZ = 10;

    /** Veri kolonları: checkbox + KEYS */
    var COL_COUNT = 1 + KEYS.length;

    function esc(s) {
        return String(s).replace(/[&<>"']/g, function (c) {
            var map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
            return map[c];
        });
    }

    function escAttr(s) {
        return String(s).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(/</g, '&lt;');
    }

    function pad(z) {
        return ('0' + z).slice(-2);
    }

    function addTriple(hostId, pref) {
        var h = document.getElementById(hostId);
        if (!h) return;
        var d = document.createElement('select');
        var m = document.createElement('select');
        var y = document.createElement('select');
        d.className = m.className = y.className = 'stok-control stok-control--date';
        d.id = 'D' + pref + 'd';
        m.id = 'D' + pref + 'm';
        y.id = 'D' + pref + 'y';
        var di;
        for (di = 1; di <= 31; di++) d.appendChild(new Option(pad(di), pad(di)));
        for (di = 1; di <= 12; di++) m.appendChild(new Option(pad(di), pad(di)));
        for (di = 2001; di <= 2038; di++) y.appendChild(new Option(String(di), String(di)));
        d.value = m.value = pad(1); y.value = '2001';
        h.appendChild(d);
        h.appendChild(document.createTextNode('.'));
        h.appendChild(m);
        h.appendChild(document.createTextNode('.'));
        h.appendChild(y);
    }
    addTriple('dBas', 'B');
    addTriple('dBit', 'E');

    function ts(pref) {
        return new Date(
            parseInt(document.getElementById('D' + pref + 'y').value, 10),
            parseInt(document.getElementById('D' + pref + 'm').value, 10) - 1,
            parseInt(document.getElementById('D' + pref + 'd').value, 10)
        ).getTime();
    }

    function defDates() {
        return document.getElementById('DBd').value === '01'
            && document.getElementById('DBm').value === '01'
            && document.getElementById('DBy').value === '2001'
            && document.getElementById('DEd').value === '01'
            && document.getElementById('DEm').value === '01'
            && document.getElementById('DEy').value === '2001';
    }

    function rowCrt(r) {
        if (r.crt != null && String(r.crt).trim()) return String(r.crt);
        if (typeof r.t === 'number') {
            var d = new Date(r.t);
            return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate());
        }
        return '';
    }

    function kodOk(val, raw) {
        var p = (raw || '').trim().toLowerCase();
        var v = (val || '').trim().toLowerCase();
        if (!p) return true;
        if (p.indexOf('*') < 0) return v === p;
        if (p.charAt(0) === '*' && p.charAt(p.length - 1) === '*' && p.length > 2)
            return v.indexOf(p.slice(1, -1)) >= 0;
        if (p.charAt(p.length - 1) === '*') return v.indexOf(p.slice(0, -1)) === 0;
        if (p.charAt(0) === '*') {
            var suf = p.slice(1);
            return v.length >= suf.length && v.substring(v.length - suf.length) === suf;
        }
        return false;
    }

    function hasSubstring(v, q) {
        if (!q) return true;
        return String(v == null ? '' : v).toLowerCase().indexOf(String(q).toLowerCase()) >= 0;
    }

    function applySearch() {
        hit = mem.filter(function (r) {
            if (!kodOk(r.model, document.getElementById('arK').value)) return false;
            var iq = document.getElementById('arI').value.trim();
            if (iq && String(r.sid || '').toLowerCase().indexOf(iq.toLowerCase()) < 0) return false;
            var k = document.getElementById('arKat').value.trim();
            if (k && !hasSubstring(r.marka + ' ' + r.kat1 + ' ' + r.kat2, k)) return false;
            var t = document.getElementById('arTan').value.trim();
            if (t && !hasSubstring(r.sad + ' ' + r.asad, t)) return false;
            var sts = document.getElementById('arSts').value;
            if (sts && r.stat !== sts) return false;
            var wb = document.getElementById('arWeb').value;
            if (wb && r.webEt !== wb) return false;
            if (!defDates() && typeof r.t === 'number') {
                var t0 = ts('B');
                var t1 = ts('E');
                var end = new Date(t1); end.setHours(23, 59, 59, 999);
                if (r.t < t0 || r.t > end.getTime()) return false;
            }
            return true;
        });
        pg = 0;
        ustBar();
    }

    function colOk(r, key) {
        var needle = key === 'crt' ? rowCrt(r) : r[key];

        var tin = txtFil[key];
        if (tin != null && String(tin).trim() !== '' && !hasSubstring(needle, String(tin).trim())) return false;
        var arr = popFil[key];
        if (arr === undefined || arr === null) return true;
        if (arr.length === 0) return false;
        var vv = String(needle == null ? '' : needle);
        return arr.indexOf(vv) >= 0;
    }

    function visibleRows() {
        return hit.filter(function (r) {
            for (var i = 0; i < KEYS.length; i++) {
                if (!colOk(r, KEYS[i])) return false;
            }
            return true;
        });
    }

    function iconTd(iconClass, title) {
        return '<td class="stok-td-ico">' +
            '<button type="button" class="stok-ico-btn" title="' + escAttr(title) + '">' +
            '<i class="bi ' + iconClass + '" aria-hidden="true"></i>' +
            '</button></td>';
    }

    function renderDataCell(r, c) {
        if (c === 'foto') return iconTd('bi-camera', 'Foto');
        if (c === 'alis') return iconTd('bi-box-arrow-in-down', 'Alış');
        if (c === 'cikis') return iconTd('bi-box-arrow-up', 'Çıkış');
        if (c === 'crt') return '<td>' + esc(rowCrt(r)) + '</td>';
        var v = r[c] == null ? '' : r[c];
        return '<td>' + esc(v) + '</td>';
    }

    function fillBody() {
        var rows = visibleRows();
        var b = document.getElementById('bod');
        if (!rows.length) {
            b.innerHTML = '<tr class="nodata"><td colspan="' + COL_COUNT + '">No data to display</td></tr>';
            return;
        }
        var s = pg * PZ;
        var sl = rows.slice(s, s + PZ);
        b.innerHTML = sl.map(function (r) {
            var sid = typeof r.sid !== 'undefined' && r.sid !== null ? String(r.sid) : '';
            var chk = !!r.chk;
            return '<tr data-sid="' + escAttr(sid) + '">' +
                '<td class="stok-td-sel">' +
                '<input type="checkbox" class="stok-row-chk" aria-label="Satır seç" ' +
                (chk ? 'checked ' : '') + '>' +
                '</td>' +
                KEYS.map(function (c) {
                    return renderDataCell(r, c);
                }).join('') +
                '</tr>';
        }).join('');
    }

    function syncHeadChk() {
        var hh = document.getElementById('chkHead');
        if (!hh) return;
        var rows = visibleRows();
        if (!rows.length) {
            hh.checked = false;
            hh.indeterminate = false;
            return;
        }
        var s = pg * PZ;
        var slice = rows.slice(s, Math.min(rows.length, s + PZ));
        if (!slice.length) {
            hh.checked = false;
            hh.indeterminate = false;
            return;
        }
        var n = slice.filter(function (r) {
            return r.chk;
        }).length;
        hh.checked = n === slice.length && slice.length > 0;
        hh.indeterminate = n > 0 && n < slice.length;
    }

    function ustBar() {
        var rows = visibleRows();
        var n = rows.length;
        var pages = n <= 0 ? 0 : Math.ceil(n / PZ);

        var pageLabel = n <= 0
            ? 'No data to paginate'
            : ((pg * PZ + 1) + ' – ' + Math.min(n, pg * PZ + PZ) + ' / ' + n);

        document.querySelectorAll('.js-pmsg').forEach(function (el) {
            el.textContent = pageLabel;
        });

        if (pg >= pages && pages > 0) pg = pages - 1;

        var disStart = n <= 0 || pg <= 0;
        var disEnd = n <= 0 || pg >= pages - 1;

        document.querySelectorAll('[data-pager="first"], [data-pager="prev"]').forEach(function (b2) {
            b2.disabled = disStart;
        });
        document.querySelectorAll('[data-pager="next"], [data-pager="last"]').forEach(function (b2) {
            b2.disabled = disEnd;
        });

        fillBody();
        syncHeadChk();
    }

    function bindPagingClicks(scope) {
        scope.addEventListener('click', function (e) {
            var btn = e.target.closest('[data-pager]');
            if (!btn || btn.disabled) return;
            var a = btn.getAttribute('data-pager');
            var cnt = visibleRows().length;
            var pMax = Math.max(1, Math.ceil(cnt / PZ));
            if (a === 'first') pg = 0;
            else if (a === 'prev') {
                if (pg > 0) pg--;
            } else if (a === 'next') {
                if (pg < pMax - 1) pg++;
            } else if (a === 'last') pg = pMax - 1;
            ustBar();
        });
    }

    function uniqForCol(key) {
        var m = {};
        var o = [];
        hit.forEach(function (r) {
            var vv = key === 'crt' ? rowCrt(r) : String(r[key] == null ? '' : r[key]);
            if (!m[vv]) {
                m[vv] = true;
                o.push(vv);
            }
        });
        o.sort(function (a2, b2) {
            if (a2 === '' && b2 !== '') return 1;
            if (b2 === '' && a2 !== '') return -1;
            return a2.localeCompare(b2, 'tr');
        });
        return o;
    }

    function recomputePopFromChecks(popBody, vals, key) {
        var cbs = popBody.querySelectorAll('input[data-v]');
        var picked = [];
        for (var ir = 0; ir < cbs.length; ir++) {
            if (cbs[ir].checked) picked.push(cbs[ir].getAttribute('data-v'));
        }
        if (!picked.length) popFil[key] = [];
        else if (picked.length === vals.length) popFil[key] = null;
        else popFil[key] = picked;
        pg = 0;
        ustBar();
    }

    function buildFilterRowOnce() {
        var tr = document.getElementById('ftr');
        tr.innerHTML = '';
        var h0 = document.createElement('th');
        h0.textContent = 'Filtrele';
        h0.className = 'ft filter-lead';
        tr.appendChild(h0);

        KEYS.forEach(function (key) {
            var th = document.createElement('th');
            th.className = 'ft';
            var wrap = document.createElement('div');
            wrap.className = 'cwrap' + (ONLY_DD[key] ? ' onlydd' : '');

            if (!ONLY_DD[key]) {
                var inp = document.createElement('input');
                inp.type = 'text';
                inp.className = 'stok-filter-txt';
                inp.addEventListener('input', function () {
                    txtFil[key] = inp.value;
                    pg = 0;
                    ustBar();
                });
                wrap.appendChild(inp);
            }

            var ddw = document.createElement('div');
            ddw.className = 'ddw';
            var bx = document.createElement('button');
            bx.type = 'button';
            bx.className = 'barr ' + (ONLY_DD[key] ? 'barr-DD' : 'barr-FT');
            bx.title = ONLY_DD[key] ? 'Liste filtresi' : 'Alan filtresi';
            bx.textContent = ONLY_DD[key] ? '\u25BC' : '\u2315';
            var pop = document.createElement('div');
            pop.className = 'pop';
            pop.addEventListener('click', function (pev) {
                pev.stopPropagation();
            });

            bx.addEventListener('click', function (ev) {
                ev.stopPropagation();
                document.querySelectorAll('.pop.open').forEach(function (x2) {
                    if (x2 !== pop) x2.classList.remove('open');
                });
                var vals = uniqForCol(key);
                var sel = popFil[key];
                pop.innerHTML = '';

                var hdr = document.createElement('div');
                hdr.style.fontWeight = '500';
                hdr.style.marginBottom = '4px';
                hdr.textContent = '(Tümü)';
                hdr.style.cursor = 'pointer';
                hdr.addEventListener('click', function () {
                    popFil[key] = null;
                    pg = 0;
                    ustBar(); pop.classList.remove('open');
                });
                pop.appendChild(hdr);

                vals.forEach(function (uv) {
                    var lab = document.createElement('label');
                    lab.className = 'pop-row';
                    var cb = document.createElement('input');
                    cb.type = 'checkbox';
                    cb.setAttribute('data-v', uv);
                    var shown = uv === '' ? '(Boş)' : uv;
                    cb.checked = (sel === undefined || sel === null) ? true : sel.indexOf(uv) >= 0;
                    var sp = document.createElement('span');
                    sp.textContent = shown;
                    lab.appendChild(cb);
                    lab.appendChild(sp);
                    cb.addEventListener('change', function () {
                        recomputePopFromChecks(pop, vals, key);
                    });
                    pop.appendChild(lab);
                });

                pop.classList.toggle('open', true);
            });

            ddw.appendChild(bx);
            ddw.appendChild(pop);
            wrap.appendChild(ddw);
            th.appendChild(wrap);
            tr.appendChild(th);
        });
    }

    document.addEventListener('click', function () {
        document.querySelectorAll('.pop.open').forEach(function (x3) {
            x3.classList.remove('open');
        });
    });

    bindPagingClicks(document.querySelector('.page-stok'));

    document.getElementById('chkHead').addEventListener('change', function () {
        var rows = visibleRows();
        var start = pg * PZ;
        var slice = rows.slice(start, Math.min(rows.length, start + PZ));
        var checked = document.getElementById('chkHead').checked;
        slice.forEach(function (r) {
            r.chk = checked;
        });
        document.querySelectorAll('#bod .stok-row-chk').forEach(function (cb, ix) {
            if (slice[ix]) cb.checked = checked;
        });
    });

    document.getElementById('bod').addEventListener('change', function (e) {
        if (!e.target.classList.contains('stok-row-chk')) return;
        var tr = e.target.closest('tr');
        if (!tr) return;
        var sidRaw = tr.getAttribute('data-sid');
        var chk = e.target.checked;
        for (var mx = 0; mx < mem.length; mx++) {
            if (String(mem[mx].sid) === sidRaw) {
                mem[mx].chk = chk;
                break;
            }
        }
        syncHeadChk();
    });

    document.getElementById('btnG').onclick = applySearch;

    document.getElementById('btnC').onclick = function () {
        document.getElementById('rAlt').value = '';
        document.getElementById('rKs').value = '';
        document.getElementById('rK1').selectedIndex = 0;
        document.getElementById('rK2').selectedIndex = 0;
        document.getElementById('rSt').selectedIndex = 0;
        document.getElementById('rWs').selectedIndex = 0;
        document.getElementById('rBr').selectedIndex = 0;
        ['ck1', 'ck2', 'ck3', 'ck4', 'ck5', 'ck6', 'ck7'].forEach(function (id) {
            document.getElementById(id).checked = true;
        });
        document.getElementById('wmsg').textContent = '';
    };

    document.getElementById('btnK').onclick = function () {
        document.getElementById('wmsg').textContent = '';
        var err = '';
        if (!document.getElementById('rAlt').value.trim())
            err += 'Alternatif Stok İsim dolu olmalı.\n';
        if (!document.getElementById('rKs').value.trim())
            err += 'Alternatif Stok Kısa İsim dolu olmalı.\n';
        if (!document.getElementById('rK1').value) err += 'İlk Kategori seçin.\n';
        if (!document.getElementById('rK2').value) err += 'İkinci Kategori seçin.\n';
        if (!document.getElementById('rSt').value) err += 'Status seçin.\n';
        if (err) {
            alert(err.trim());
            document.getElementById('wmsg').textContent = err.replace(/\n+/g, ' ');
            return;
        }

        var kod = document.getElementById('arK').value.trim();
        var A = document.getElementById('rAlt').value.trim();
        var ks = document.getElementById('rKs').value.trim();
        var sts = document.getElementById('rSt').value;
        var ws = document.getElementById('rWs').value;
        var br = document.getElementById('rBr').value;
        var arW = document.getElementById('arWeb').value || ws;

        var nowMs = Date.now();
        var d = new Date(nowMs);

        mem.push({
            foto: '',
            alis: '—',
            cikis: '—',
            orj: kod ? kod + '-ORJ' : '-',
            model: kod || A.slice(0, 16),
            marka: document.getElementById('arKat').value.trim() || '-',
            sad: A, asad: A, ksa: ks, aksi: ks,
            fy1: '0', fy2: '0', fy3: '0', fy10: '0',
            rek: br === 'Rektifiye' ? 'Evet' : 'Hayır',
            bak: br === 'Bakım' ? 'Evet' : 'Hayır',
            onl: ws.indexOf('Görünmesin') >= 0 ? 'Hayır' : 'Evet',
            kat1: document.getElementById('rK1').value,
            kat2: document.getElementById('rK2').value,
            dmr: '0', dmt: '0',
            sid: document.getElementById('arI').value.trim() || ('AUTO-' + (mem.length + 1)),
            chk: false,
            stat: sts,
            webEt: arW || ws,
            t: nowMs,
            crt: d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()),
        });

        applySearch();
        document.getElementById('btnC').click();
    };

    try {
        var qs = new URLSearchParams(window.location.search);
        var pq = qs.get('q');
        if (pq) {
            var arKatEl = document.getElementById('arKat');
            if (arKatEl) arKatEl.value = pq;
        }
    } catch (ignore2) {}

    buildFilterRowOnce();
    hit = [];
    applySearch();
})();

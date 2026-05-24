/**
 * Anasayfa FullCalendar v5 kurulumu, localStorage etkinlikler, Not dock.
 */
(function () {
    var STORAGE_EVENTS = "sase.dashboard.calendar.events";
    var STORAGE_NOTE_PREFIX = "sase.dashboard.calendar.note.";
    var calEl = document.getElementById("dashCalendar");
    if (!calEl || typeof FullCalendar === "undefined") return;

    var userId =
        typeof window.__DASH_USER_ID === "string" && window.__DASH_USER_ID
            ? window.__DASH_USER_ID
            : "default";

    function eventsStorageKey() {
        return STORAGE_EVENTS + "." + userId;
    }

    function noteStorageKey() {
        return STORAGE_NOTE_PREFIX + userId;
    }

    function loadPersistedEvents() {
        try {
            var raw = localStorage.getItem(eventsStorageKey());
            if (!raw) return [];
            var arr = JSON.parse(raw);
            return Array.isArray(arr) ? arr : [];
        } catch (e) {
            return [];
        }
    }

    function saveEventsFromCalendar(calendar) {
        var list = calendar.getEvents().map(function (ev) {
            return {
                id: ev.id,
                title: ev.title,
                start: ev.start ? ev.start.toISOString() : null,
                end: ev.end ? ev.end.toISOString() : null,
                allDay: !!ev.allDay,
            };
        });
        try {
            localStorage.setItem(eventsStorageKey(), JSON.stringify(list));
        } catch (e) {
            /* quota */
        }
    }

    var titleEl = document.getElementById("homeCalTitle");
    var btnPrev = document.getElementById("homeCalPrev");
    var btnNext = document.getElementById("homeCalNext");
    var btnToday = document.getElementById("homeCalToday");
    var fabPrev = document.getElementById("homeCalFabPrev");
    var fabNext = document.getElementById("homeCalFabNext");
    var viewBtns = document.querySelectorAll("[data-cal-view]");

    var calendar = new FullCalendar.Calendar(calEl, {
        locale: "tr",
        firstDay: 1,
        initialView: "dayGridMonth",
        headerToolbar: false,
        height: "auto",
        navLinks: true,
        selectable: true,
        nowIndicator: true,
        editable: true,
        eventDurationEditable: true,
        dayMaxEvents: true,
        buttonText: {
            today: "Bugün",
            month: "Ay",
            week: "Hafta",
            day: "Gün",
            list: "Liste",
        },
        views: {
            timeGridWorkWeek: {
                type: "timeGridWeek",
                hiddenDays: [0, 6],
                buttonText: { week: "İş Haftası" },
            },
        },
        events: loadPersistedEvents().map(function (x) {
            return {
                id: x.id,
                title: x.title,
                start: x.start,
                end: x.end || undefined,
                allDay: x.allDay,
            };
        }),
        datesSet: function (info) {
            if (titleEl) titleEl.textContent = info.view.title;
            setActiveViewButton(info.view.type);
        },
        dateClick: function (info) {
            var title = window.prompt("Etkinlik başlığı girin (boş bırakırsanız iptal):");
            if (!title || !String(title).trim()) return;
            calendar.addEvent({
                id: String(Date.now()) + "-" + Math.random().toString(36).slice(2, 8),
                title: String(title).trim(),
                start: info.date,
                allDay: info.allDay,
            });
            saveEventsFromCalendar(calendar);
        },
        eventClick: function (info) {
            if (window.confirm("Bu etkinliği silinsin mi?")) {
                info.event.remove();
                saveEventsFromCalendar(calendar);
            }
        },
        eventDrop: function () {
            saveEventsFromCalendar(calendar);
        },
        eventResize: function () {
            saveEventsFromCalendar(calendar);
        },
    });

    calendar.render();

    function setActiveViewButton(viewType) {
        viewBtns.forEach(function (b) {
            b.classList.toggle("is-active", b.getAttribute("data-cal-view") === viewType);
        });
    }

    function goView(viewName) {
        calendar.changeView(viewName);
        setActiveViewButton(viewName);
    }

    btnPrev &&
        btnPrev.addEventListener("click", function () {
            calendar.prev();
        });
    btnNext &&
        btnNext.addEventListener("click", function () {
            calendar.next();
        });
    fabPrev &&
        fabPrev.addEventListener("click", function () {
            calendar.prev();
        });
    fabNext &&
        fabNext.addEventListener("click", function () {
            calendar.next();
        });
    btnToday &&
        btnToday.addEventListener("click", function () {
            calendar.today();
        });

    viewBtns.forEach(function (btn) {
        btn.addEventListener("click", function () {
            goView(btn.getAttribute("data-cal-view"));
        });
    });

    /* —— Not panel —— */
    var dock = document.getElementById("homeNoteDock");
    var noteFab = document.getElementById("homeNoteFab");
    var notePanel = document.getElementById("homeNotePanel");
    var noteClose = document.getElementById("homeNoteClose");
    var noteTa = document.getElementById("homeNoteTextarea");

    function openNote(open) {
        if (!dock) return;
        dock.classList.toggle("is-open", open);
        if (open && noteTa) {
            noteTa.focus();
        }
    }

    if (noteTa) {
        try {
            noteTa.value = localStorage.getItem(noteStorageKey()) || "";
        } catch (e) {}

        var saveTimer;
        noteTa.addEventListener("input", function () {
            clearTimeout(saveTimer);
            saveTimer = setTimeout(function () {
                try {
                    localStorage.setItem(noteStorageKey(), noteTa.value);
                } catch (e) {}
            }, 300);
        });
    }

    noteFab &&
        noteFab.addEventListener("click", function () {
            openNote(!dock.classList.contains("is-open"));
        });

    noteClose &&
        noteClose.addEventListener("click", function () {
            openNote(false);
        });

    document.addEventListener("keydown", function (ev) {
        if (ev.key === "Escape" && dock && dock.classList.contains("is-open")) {
            openNote(false);
        }
    });
})();

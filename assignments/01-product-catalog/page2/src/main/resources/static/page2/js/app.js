// ===== Third-party API: Rick and Morty =====
const API = "https://rickandmortyapi.com/api";
const DELAY_MS = 4000; // search delay: 4 seconds

const $search = document.getElementById("search");
const $grid = document.getElementById("grid");
const $status = document.getElementById("status");
const $prev = document.getElementById("prev");
const $next = document.getElementById("next");
const $pageInfo = document.getElementById("pageInfo");
const $title = document.getElementById("modalTitle");
const $body = document.getElementById("modalBody");
const modal = new bootstrap.Modal(document.getElementById("detailModal"));

const STATUS_CLASS = { Alive: "text-bg-success", Dead: "text-bg-danger", unknown: "text-bg-secondary" };
const dateFmt = new Intl.DateTimeFormat("en-US", { dateStyle: "long", timeZone: "UTC" });

let characters = [], info = null, page = 1, ctrl = null, timer = null, tick = null, openId = null;

// Escape HTML: data from a third-party API must never be inserted raw (XSS)
const escapeHtml = (s) => String(s ?? "").replace(/[&<>"']/g, (c) =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const statusClass = (c) => STATUS_CLASS[c.status] || STATUS_CLASS.unknown;

// ===== Load from the API (fetch + async/await) =====
async function search(num = 1) {
  if (ctrl) ctrl.abort(); // cancel a pending older request
  ctrl = new AbortController();
  page = num;
  const name = $search.value.trim();
  $status.textContent = "Loading characters…";
  try {
    const res = await fetch(`${API}/character?page=${num}&name=${encodeURIComponent(name)}`, { signal: ctrl.signal });
    if (res.status === 404) { characters = []; info = null; render(); $status.textContent = "No characters found with that name."; return; }
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data = await res.json();
    characters = data.results; info = data.info;
    render();
    $status.textContent = `${info.count} characters found`;
  } catch (err) {
    if (err.name === "AbortError") return;
    $status.textContent = "Could not reach the Rick and Morty API. Check your connection and try again.";
  }
}

// ===== Search with a 4-second delay (debounce) and a visible countdown =====
function scheduleSearch() {
  clearTimeout(timer); clearInterval(tick);
  let sec = DELAY_MS / 1000;
  $status.textContent = `Searching in ${sec} s…`;
  tick = setInterval(() => { sec--; if (sec > 0) $status.textContent = `Searching in ${sec} s…`; }, 1000);
  timer = setTimeout(() => { clearInterval(tick); search(1); }, DELAY_MS);
}

// ===== Cards =====
function card(c) {
  return `
  <article class="character-card" data-id="${c.id}" tabindex="0" role="button" aria-label="View details of ${escapeHtml(c.name)}">
    <img src="${escapeHtml(c.image)}" alt="Picture of ${escapeHtml(c.name)}" loading="lazy">
    <div class="body">
      <div class="tw-flex tw-items-start tw-justify-between tw-gap-2">
        <h2>${escapeHtml(c.name)}</h2><span class="badge ${statusClass(c)}">${escapeHtml(c.status)}</span>
      </div>
      <dl class="data">
        <dt>Species</dt><dd>${escapeHtml(c.species)}</dd>
        <dt>Type</dt><dd>${escapeHtml(c.type) || "—"}</dd>
        <dt>Gender</dt><dd>${escapeHtml(c.gender)}</dd>
        <dt>Origin</dt><dd>${escapeHtml(c.origin.name)}</dd>
        <dt>Location</dt><dd>${escapeHtml(c.location.name)}</dd>
        <dt>Episodes</dt><dd>${c.episode.length}</dd>
      </dl>
    </div>
  </article>`;
}

function render() {
  $grid.innerHTML = characters.map(card).join("");
  $prev.disabled = !info || !info.prev;
  $next.disabled = !info || !info.next;
  $pageInfo.textContent = info ? `Page ${page} of ${info.pages}` : "";
}

// ===== Full details (overlay window) =====
async function openDetail(id) {
  const c = characters.find((x) => x.id === id);
  if (!c) return;
  openId = id;
  $title.textContent = c.name;
  $body.innerHTML = `
    <div class="tw-flex tw-flex-col tw-gap-4 detail-flex">
      <img src="${escapeHtml(c.image)}" alt="Picture of ${escapeHtml(c.name)}" class="tw-rounded-lg tw-w-full tw-self-start">
      <div class="tw-flex-1">
        <span class="badge ${statusClass(c)} tw-mb-3">${escapeHtml(c.status)}</span>
        <dl class="data">
          <dt>ID</dt><dd>${c.id}</dd>
          <dt>Species</dt><dd>${escapeHtml(c.species)}</dd>
          <dt>Type</dt><dd>${escapeHtml(c.type) || "None"}</dd>
          <dt>Gender</dt><dd>${escapeHtml(c.gender)}</dd>
          <dt>Origin</dt><dd>${escapeHtml(c.origin.name)}</dd>
          <dt>Last location</dt><dd>${escapeHtml(c.location.name)}</dd>
          <dt>Created in API</dt><dd>${dateFmt.format(new Date(c.created))}</dd>
          <dt>Appearances</dt><dd>${c.episode.length} episodes</dd>
        </dl>
        <h3 class="fs-6 mt-3">Episodes</h3>
        <ul id="episodeList" class="episodes"><li>Loading episodes…</li></ul>
      </div>
    </div>`;
  modal.show();
  try {
    const ids = c.episode.map((u) => u.split("/").pop()).join(",");
    const res = await fetch(`${API}/episode/${ids}`);
    if (!res.ok) throw new Error();
    const eps = [].concat(await res.json()); // the API returns an object for 1 episode, an array for several
    if (openId !== id) return;
    document.getElementById("episodeList").innerHTML =
      eps.map((e) => `<li>${escapeHtml(e.episode)} · ${escapeHtml(e.name)} <span class="text-secondary">(${escapeHtml(e.air_date)})</span></li>`).join("");
  } catch {
    const l = document.getElementById("episodeList");
    if (l && openId === id) l.innerHTML = "<li>Could not load episodes.</li>";
  }
}

// ===== Events =====
$search.addEventListener("input", scheduleSearch);
$prev.addEventListener("click", () => search(page - 1));
$next.addEventListener("click", () => search(page + 1));
$grid.addEventListener("click", (e) => { const el = e.target.closest("[data-id]"); if (el) openDetail(Number(el.dataset.id)); });
$grid.addEventListener("keydown", (e) => {
  const el = e.target.closest("[data-id]");
  if (el && (e.key === "Enter" || e.key === " ")) { e.preventDefault(); openDetail(Number(el.dataset.id)); }
});

search(1); // initial load without waiting

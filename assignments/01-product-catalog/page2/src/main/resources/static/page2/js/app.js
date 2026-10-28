// ===== APIs =====
// Characters now come from OUR backend (/api/characters), which calls Rick and Morty for us.
// The path is relative ("../api") so it also works behind the gateway under /page2/.
const BACKEND = "../api";
// Episodes are still read straight from the public API (only used in the detail window).
const API = "https://rickandmortyapi.com/api";
const DELAY_MS = 4000; // search delay: 4 seconds

const $search = document.getElementById("search");
const $grid = document.getElementById("grid");
const $status = document.getElementById("status");
const $pager = document.getElementById("pager");
const $prev = document.getElementById("prev");
const $next = document.getElementById("next");
const $pageInfo = document.getElementById("pageInfo");
const $tabAll = document.getElementById("tabAll");
const $tabFav = document.getElementById("tabFav");
const $favCount = document.getElementById("favCount");
const $title = document.getElementById("modalTitle");
const $modalFav = document.getElementById("modalFav");
const $body = document.getElementById("modalBody");
const modal = new bootstrap.Modal(document.getElementById("detailModal"));

const STATUS_CLASS = { Alive: "text-bg-success", Dead: "text-bg-danger", unknown: "text-bg-secondary" };
const dateFmt = new Intl.DateTimeFormat("en-US", { dateStyle: "long", timeZone: "UTC" });

let characters = [], info = null, page = 1, ctrl = null, timer = null, tick = null, openId = null;
let view = "all";            // "all" = search results, "fav" = favorites tab
let favorites = new Map();   // characterId -> favorite saved in our database

// Escape HTML: data from a third-party API must never be inserted raw (XSS)
const escapeHtml = (s) => String(s ?? "").replace(/[&<>"']/g, (c) =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const res502 = (err) => err && err.cause === 502; // our backend says the external API failed
const statusClass = (c) => STATUS_CLASS[c.status] || STATUS_CLASS.unknown;
const isFav = (id) => favorites.has(id);

// ===== Load from the API (fetch + async/await) =====
async function search(num = 1) {
  if (ctrl) ctrl.abort(); // cancel a pending older request
  ctrl = new AbortController();
  page = num;
  const name = $search.value.trim();
  $status.textContent = "Loading characters…";
  try {
    const res = await fetch(`${BACKEND}/characters?page=${num}&name=${encodeURIComponent(name)}`, { signal: ctrl.signal });
    if (res.status === 404) { characters = []; info = null; render(); $status.textContent = "No characters found with that name."; return; }
    if (!res.ok) throw new Error(`HTTP ${res.status}`, { cause: res.status });
    const data = await res.json();
    characters = data.results; info = data.info;
    render();
    $status.textContent = `${info.count} characters found`;
  } catch (err) {
    if (err.name === "AbortError") return;
    $status.textContent = res502(err) ? "The Rick and Morty API is not answering right now. Try again in a moment." : "Could not reach the server. Check your connection and try again.";
  }
}

// ===== Search with a 4-second delay (debounce) and a visible countdown =====
function scheduleSearch() {
  if (view !== "all") showTab("all");
  clearTimeout(timer); clearInterval(tick);
  let sec = DELAY_MS / 1000;
  $status.textContent = `Searching in ${sec} s…`;
  tick = setInterval(() => { sec--; if (sec > 0) $status.textContent = `Searching in ${sec} s…`; }, 1000);
  timer = setTimeout(() => { clearInterval(tick); search(1); }, DELAY_MS);
}

// ===== Favorites (saved in PostgreSQL through /api/favorites) =====
async function loadFavorites() {
  try {
    const res = await fetch(`${BACKEND}/favorites`);
    if (!res.ok) throw new Error();
    favorites = new Map((await res.json()).map((f) => [f.characterId, f]));
  } catch {
    favorites = new Map(); // the page still works, only without favorites
  }
  updateFavCount();
}

// Adds or removes a favorite. c can be a full character (search) or a saved favorite.
async function toggleFavorite(c) {
  // A saved favorite has characterId (its own "id" is the database row); a character from the API only has id
  const id = c.characterId ?? c.id;
  try {
    if (isFav(id)) {
      const res = await fetch(`${BACKEND}/favorites/${id}`, { method: "DELETE" });
      if (!res.ok && res.status !== 404) throw new Error();
      favorites.delete(id);
    } else {
      const res = await fetch(`${BACKEND}/favorites`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ characterId: id, name: c.name, image: c.image, status: c.status, species: c.species }),
      });
      if (!res.ok && res.status !== 409) throw new Error();
      favorites.set(id, res.status === 409 ? { characterId: id, ...c } : await res.json());
    }
  } catch {
    $status.textContent = "Could not update favorites. Try again.";
    return;
  }
  updateFavCount();
  if (openId === id) setModalFav(id);
  render();
}

function updateFavCount() { $favCount.textContent = favorites.size; }

const heart = (id, name) => `
  <button type="button" class="fav-btn ${isFav(id) ? "is-fav" : ""}" data-fav="${id}"
          aria-pressed="${isFav(id)}" aria-label="${isFav(id) ? "Remove" : "Add"} ${escapeHtml(name)} ${isFav(id) ? "from" : "to"} favorites">
    ${isFav(id) ? "♥" : "♡"}
  </button>`;

// ===== Cards =====
function card(c) {
  return `
  <article class="character-card" data-id="${c.id}" tabindex="0" role="button" aria-label="View details of ${escapeHtml(c.name)}">
    <div class="img-wrap">
      <img src="${escapeHtml(c.image)}" alt="Picture of ${escapeHtml(c.name)}" loading="lazy">
      ${heart(c.id, c.name)}
    </div>
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

// Smaller card for the Favorites tab (we only saved name, image, status and species)
function favCard(f) {
  return `
  <article class="character-card" data-id="${f.characterId}" tabindex="0" role="button" aria-label="View details of ${escapeHtml(f.name)}">
    <div class="img-wrap">
      <img src="${escapeHtml(f.image)}" alt="Picture of ${escapeHtml(f.name)}" loading="lazy">
      ${heart(f.characterId, f.name)}
    </div>
    <div class="body">
      <div class="tw-flex tw-items-start tw-justify-between tw-gap-2">
        <h2>${escapeHtml(f.name)}</h2><span class="badge ${statusClass(f)}">${escapeHtml(f.status || "unknown")}</span>
      </div>
      <dl class="data"><dt>Species</dt><dd>${escapeHtml(f.species) || "—"}</dd></dl>
    </div>
  </article>`;
}

function render() {
  if (view === "fav") {
    const list = [...favorites.values()];
    $grid.innerHTML = list.length
      ? list.map(favCard).join("")
      : `<p class="empty">You have no favorites yet. Tap the ♡ on any character to save it here.</p>`;
    $pager.hidden = true;
    return;
  }
  $grid.innerHTML = characters.map(card).join("");
  $pager.hidden = false;
  $prev.disabled = !info || !info.prev;
  $next.disabled = !info || !info.next;
  $pageInfo.textContent = info ? `Page ${page} of ${info.pages}` : "";
}

function showTab(tab) {
  view = tab;
  $tabAll.classList.toggle("active", tab === "all");
  $tabFav.classList.toggle("active", tab === "fav");
  $tabAll.setAttribute("aria-selected", tab === "all");
  $tabFav.setAttribute("aria-selected", tab === "fav");
  if (tab === "fav") $status.textContent = `${favorites.size} favorite${favorites.size === 1 ? "" : "s"}`;
  else $status.textContent = info ? `${info.count} characters found` : "";
  render();
}

// ===== Full details (overlay window) =====
function setModalFav(id) {
  $modalFav.classList.toggle("is-fav", isFav(id));
  $modalFav.textContent = isFav(id) ? "♥ In favorites" : "♡ Add to favorites";
  $modalFav.setAttribute("aria-pressed", isFav(id));
}

async function openDetail(id) {
  // From the search results we already have the character; from Favorites we ask our backend
  let c = characters.find((x) => x.id === id);
  if (!c) {
    try {
      const res = await fetch(`${BACKEND}/characters/${id}`);
      if (!res.ok) throw new Error();
      c = await res.json();
    } catch {
      $status.textContent = "Could not load that character.";
      return;
    }
  }
  openId = id;
  $title.textContent = c.name;
  setModalFav(id);
  $modalFav.onclick = () => toggleFavorite(c);
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
$tabAll.addEventListener("click", () => showTab("all"));
$tabFav.addEventListener("click", () => showTab("fav"));
$grid.addEventListener("click", (e) => {
  const favBtn = e.target.closest("[data-fav]");
  if (favBtn) { // the heart only toggles the favorite, it does not open the details
    e.stopPropagation();
    const id = Number(favBtn.dataset.fav);
    toggleFavorite(characters.find((x) => x.id === id) || favorites.get(id));
    return;
  }
  const el = e.target.closest("[data-id]");
  if (el) openDetail(Number(el.dataset.id));
});
$grid.addEventListener("keydown", (e) => {
  if (e.target.closest("[data-fav]")) return; // Enter on the heart is a normal button click
  const el = e.target.closest("[data-id]");
  if (el && (e.key === "Enter" || e.key === " ")) { e.preventDefault(); openDetail(Number(el.dataset.id)); }
});
document.getElementById("detailModal").addEventListener("hidden.bs.modal", () => { openId = null; });

loadFavorites().then(() => search(1)); // initial load without waiting

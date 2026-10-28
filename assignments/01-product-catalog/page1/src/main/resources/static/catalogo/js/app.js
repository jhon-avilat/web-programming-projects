// ===== Endpoint (same Spring Boot server, so a relative URL works and CORS is not needed) =====
const API_URL = "/api/productos";

const $search = document.getElementById("search");
const $suggestions = document.getElementById("suggestions");
const $grid = document.getElementById("grid");
const $counter = document.getElementById("counter");
const $notice = document.getElementById("notice");

const money = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });
const dateFmt = new Intl.DateTimeFormat("en-US", { dateStyle: "medium", timeZone: "UTC" });

let products = [];
let activeIndex = -1;

// ===== Utilities =====
// fold: lowercase and strip accents while keeping the text length (needed for highlighting)
const fold = (s) => Array.from(String(s ?? "")).map((c) => {
  const f = c.normalize("NFD")[0].toLowerCase();
  return f.length === 1 ? f : c;
}).join("");

// Escape HTML to prevent XSS before inserting text with innerHTML
const escapeHtml = (s) => String(s ?? "").replace(/[&<>"']/g, (c) =>
  ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

// Wrap every searched word found in the text with <mark>
function highlight(text, query) {
  const original = String(text ?? "");
  const plain = fold(original);
  const marked = new Array(original.length).fill(false);
  query.split(/\s+/).filter(Boolean).forEach((t) => {
    for (let i = plain.indexOf(t); i !== -1; i = plain.indexOf(t, i + 1)) {
      for (let j = i; j < i + t.length; j++) marked[j] = true;
    }
  });
  return Array.from(original).map((c, i) =>
    marked[i] ? `<mark>${escapeHtml(c)}</mark>` : escapeHtml(c)).join("").replace(/<\/mark><mark>/g, "");
}

// ===== Data loading: fetch + async/await, with a fallback if the endpoint is down =====
async function loadProducts() {
  const ctrl = new AbortController();
  const timer = setTimeout(() => ctrl.abort(), 3000);
  try {
    const res = await fetch(API_URL, { signal: ctrl.signal });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    $notice.innerHTML = `<div class="alert alert-warning py-2">Could not reach <code>${API_URL}</code>. Showing demo data.</div>`;
    return DEMO_PRODUCTS;
  } finally {
    clearTimeout(timer);
  }
}

// ===== Instant search (runs on every keystroke, no delay) =====
const searchText = (p) => fold(`${p.nomProducto} ${p.codigo} ${p.descripcion}`);

function filterProducts(query) {
  const tokens = fold(query).split(/\s+/).filter(Boolean);
  return products.filter((p) => { const t = searchText(p); return tokens.every((k) => t.includes(k)); });
}

function stockStatus(stock) {
  if (stock <= 0) return { text: "Out of stock", cls: "text-bg-danger" };
  if (stock <= 5) return { text: "Low stock", cls: "text-bg-warning" };
  return { text: "In stock", cls: "text-bg-success" };
}

function card(p, q) {
  const s = stockStatus(Number(p.stock));
  return `
  <article class="product-card">
    <div class="tw-flex tw-items-start tw-justify-between tw-gap-2">
      <h2>${highlight(p.nomProducto, q)}</h2>
      <span class="badge ${s.cls}">${s.text}</span>
    </div>
    <p class="tw-text-sm tw-text-slate-500 tw-m-0">Code: ${highlight(p.codigo, q)}</p>
    <p class="tw-text-sm tw-text-slate-700 tw-m-0">${highlight(p.descripcion, q)}</p>
    <dl class="details">
      <dt>Price</dt><dd>${money.format(Number(p.precio))}</dd>
      <dt>Stock</dt><dd>${Number(p.stock)} units</dd>
      <dt>Added</dt><dd>${p.fechIngreso ? dateFmt.format(new Date(p.fechIngreso)) : "—"}</dd>
      <dt>Product no.</dt><dd>${p.numProducto}</dd>
    </dl>
  </article>`;
}

function render() {
  const q = $search.value.trim();
  const list = filterProducts(q);
  $counter.textContent = q ? `${list.length} result(s) for "${q}"` : `${list.length} products`;
  $grid.innerHTML = list.length
    ? list.map((p) => card(p, fold(q))).join("")
    : `<p class="tw-text-slate-600">No products match your search. Try another word or check the code.</p>`;
}

// ===== Predictive suggestions =====
function suggest() {
  const q = fold($search.value.trim());
  if (!q) return closeSuggestions();
  const matches = products
    .map((p) => ({ p, pos: fold(p.nomProducto).indexOf(q) }))
    .filter((x) => x.pos !== -1)
    .sort((a, b) => a.pos - b.pos)
    .slice(0, 5);
  activeIndex = -1;
  if (!matches.length) return closeSuggestions();
  $suggestions.innerHTML = matches.map(({ p }, i) =>
    `<li class="list-group-item" role="option" id="sug-${i}" data-name="${escapeHtml(p.nomProducto)}">
       <span>${highlight(p.nomProducto, q)}</span><small class="text-secondary">${escapeHtml(p.codigo)}</small></li>`).join("");
  $suggestions.hidden = false;
  $search.setAttribute("aria-expanded", "true");
}

function closeSuggestions() {
  $suggestions.hidden = true;
  $search.setAttribute("aria-expanded", "false");
  activeIndex = -1;
}

function pick(name) {
  $search.value = name;
  closeSuggestions();
  render();
}

function moveActive(delta) {
  const items = [...$suggestions.children];
  if (!items.length) return;
  activeIndex = (activeIndex + delta + items.length) % items.length;
  items.forEach((li, i) => li.classList.toggle("active-item", i === activeIndex));
  $search.setAttribute("aria-activedescendant", items[activeIndex].id);
}

$search.addEventListener("input", () => { render(); suggest(); });
$search.addEventListener("keydown", (e) => {
  if (e.key === "ArrowDown") { e.preventDefault(); moveActive(1); }
  else if (e.key === "ArrowUp") { e.preventDefault(); moveActive(-1); }
  else if (e.key === "Enter" && activeIndex >= 0) { e.preventDefault(); pick($suggestions.children[activeIndex].dataset.name); }
  else if (e.key === "Escape") closeSuggestions();
});
// mousedown (not click): the input's blur fires before click and would close the list first
$suggestions.addEventListener("mousedown", (e) => {
  const li = e.target.closest("li");
  if (li) pick(li.dataset.name);
});
$search.addEventListener("blur", closeSuggestions);

// ===== Start =====
(async () => { products = await loadProducts(); render(); })();

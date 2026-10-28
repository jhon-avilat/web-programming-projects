# ProgramacionWebPage2 — Product CRUD + Rick and Morty (Third-party API)

## What this project does

It keeps the boilerplate product CRUD and adds a page, written in **vanilla JavaScript**, that consumes the public [Rick and Morty API](https://rickandmortyapi.com/): a responsive character browser with images, pagination, a search with a 4-second delay and a modal with the full details of each character.

## Requirements checklist

| Requirement | Where it is implemented |
|-------------|-------------------------|
| Third-party API endpoints | `static/page2/js/app.js` → `GET /character?page=&name=` and `GET /episode/{ids}` on `https://rickandmortyapi.com/api` |
| Vanilla JavaScript (no framework) | `static/page2/js/app.js` |
| Responsive cards | `css/styles.css`: `.characters-grid` uses 1 / 2 / 3 / 4 columns from 0 / 576 / 992 / 1400 px (media queries) |
| Cards with image and all the data | `card()` in `app.js`: image, name, status, species, type, gender, origin, location, number of episodes |
| Click opens the full details | Bootstrap modal (`#detailModal`): large image, every field, creation date and the complete episode list. Works with mouse and keyboard (Enter / Space) |
| Search with a 4-second delay | `DELAY_MS = 4000` and `scheduleSearch()` in `app.js`, with a visible countdown |
| Bootstrap | `index.html` (Bootstrap 5.3.3 CDN): navbar, modal, badges, buttons |
| CSS Grid / Flexbox | Grid: `.characters-grid`, `.data`. Flexbox: `.header-flex`, `.character-card`, modal layout |
| Tailwind CSS | Tailwind 3.4.17 (CDN) with prefix `tw-` and preflight disabled, so it does not clash with Bootstrap |

## Technology stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.1 |
| Web layer | Spring Web MVC (`@Controller` for the Thymeleaf CRUD) |
| Persistence | Spring Data JPA + Hibernate (`ddl-auto=update`) |
| Database | PostgreSQL |
| Templates | Thymeleaf (CRUD pages use Bootstrap 4.1.3 from the boilerplate) |
| Build tool | Maven (Maven Wrapper included) |
| Rick and Morty page | Vanilla JavaScript, Bootstrap 5.3.3 (CDN), Tailwind CSS 3.4.17 (CDN, prefix `tw-`) |
| Fonts | IBM Plex Sans, Space Grotesk (Google Fonts) |
| Third-party API | Rick and Morty API (public, no key needed) |
| IDE | NetBeans (`nbactions.xml` included) |

## Project structure

```
page2/   (ProgramacionWebPage2)
├── pom.xml
├── products_seed.sql                          ← 20 sample products (one INSERT)
├── README.md
└── src/main/
    ├── java/uce/edu/ProgramacionWeb/
    │   ├── ProgramacionWebApplication.java    ← Spring Boot entry point
    │   ├── Controller/ProductoController.java ← Thymeleaf CRUD routes
    │   ├── Model/Producto.java                ← JPA entity → table "producto"
    │   ├── Repository/ProductoRepository.java
    │   └── Service/ProductoService.java
    └── resources/
        ├── application.properties             ← includes server.port=8081
        ├── templates/                         ← Thymeleaf pages (CRUD)
        │   ├── index.html                     ← main menu (with the "Rick and Morty" button)
        │   ├── listar_productos.html
        │   ├── nuevo_producto.html
        │   └── actualizar_producto.html
        └── static/page2/                      ← Rick and Morty page (new)
            ├── index.html
            ├── css/styles.css
            └── js/app.js
```

## What was added on top of the boilerplate

### 1. `templates/index.html` — "Rick and Morty" button
```html
<a th:href="@{/page2/index.html}" class="btn btn-success btn-sm mb-3">Rick and Morty (Page 2)</a>
```

### 2. `application.properties` — own port
`server.port=8081` lets this project run at the same time as Page 1 (port 8080).

### 3. `static/page2/` — the Rick and Morty page
Spring Boot serves it as a static resource and the browser talks directly to the public API, so no new Java controller was needed.

#### `js/app.js`
- **Initial load:** the first page of characters is fetched immediately when the page opens.
- **Search with a 4-second delay (debounce):** every keystroke restarts a 4-second timer and a countdown ("Searching in 3 s…"); the API is called only when the timer ends.
  ```
  keystroke → clearTimeout → countdown (4 s) → fetch the API
  ```
- **Request cancellation:** each request uses an `AbortController`; a new search aborts the previous one, so old answers cannot overwrite newer ones.
- **Pagination:** Previous / Next buttons and a "Page X of Y" label (20 characters per page).
- **Cards:** lazy-loaded image, name, status badge (Alive = green, Dead = red, unknown = grey), species, type, gender, origin, location and episode count.
- **Modal:** large image, ID, species, type, gender, origin, last location, creation date (`Intl.DateTimeFormat`), number of appearances, and the full episode list (code, name, air date) loaded with a second request to `/episode/id1,id2,…`.
- **Security:** all API data goes through `escapeHtml()` before being inserted as HTML.
- **Error handling:** no results (HTTP 404) → "No characters found with that name."; network error → connection message; episode error → message inside the modal.
- **Navigation:** a "Back to menu" button returns to `/`.

#### `css/styles.css`
Custom properties for colors; Flexbox header; CSS Grid for the cards (1 to 4 columns with media queries); card hover/focus styles; a side-by-side modal layout from 768 px up (stacked on phones).

## How to run

### Prerequisites
- JDK 17 or newer
- PostgreSQL running on `localhost:5432`
- **Internet connection** (the page calls an external API)
- NetBeans (or any IDE / terminal with Maven)

### 1. Create the database
```sql
CREATE DATABASE "ProgramacionWebPage2";
```

### 2. Check credentials
`src/main/resources/application.properties`:
```properties
spring.application.name=ProgramacionWebPage2
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/ProgramacionWebPage2
spring.datasource.username=postgres
spring.datasource.password=1234
```
The password is a local-development placeholder: use your own.

### 3. Run
From NetBeans: right-click the project → **Run**. From a terminal:
```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

### 4. Load sample data (optional)
Run `products_seed.sql` in pgAdmin on the `ProgramacionWebPage2` database after the first start.

### 5. Open it
| URL | What you see |
|-----|--------------|
| http://localhost:8081 | Main menu (Thymeleaf) |
| http://localhost:8081/listarProductos | Product list (CRUD) |
| http://localhost:8081/page2/index.html | Rick and Morty page |

## Rick and Morty API reference

| Endpoint | Used for |
|----------|----------|
| `GET https://rickandmortyapi.com/api/character?page={n}&name={q}` | Search and paginate characters |
| `GET https://rickandmortyapi.com/api/episode/{id1},{id2},…` | Episode list of one character |

## Entity — `Producto`

| Java field | DB column | Type |
|------------|-----------|------|
| `numProducto` | `num_producto` | `BIGINT` (primary key, auto-generated) |
| `codigo` | `codigo` | `VARCHAR` |
| `nomProducto` | `nom_producto` | `VARCHAR` |
| `descripcion` | `descripcion` | `VARCHAR` |
| `precio` | `precio` | `NUMERIC` |
| `stock` | `stock` | `INTEGER` |
| `fechIngreso` | `fech_ingreso` | `DATE` |

The class and field names keep the Spanish naming style of the course boilerplate; Hibernate maps them to snake_case columns.

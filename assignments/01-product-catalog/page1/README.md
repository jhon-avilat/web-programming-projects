# ProgramacionWebPage1 — Product CRUD + REST API + Instant-search Catalog

> **Course:** Web Programming · **University:** UCE
> **Database:** `ProgramacionWebPage1` (PostgreSQL)
> **Main entry point:** http://localhost:8080

## What this project does

It extends the course boilerplate (Spring Boot + Thymeleaf + PostgreSQL CRUD) with:

1. A **REST API** at `/api/productos` that exposes the product table as JSON.
2. An **instant-search catalog** at `/catalogo/index.html`, written in **vanilla JavaScript**, that consumes that API.

## Requirements checklist

| Requirement | Where it is implemented |
|-------------|-------------------------|
| Endpoints | `ProductoRestController.java` → `/api/productos` (GET, POST, PUT, DELETE) |
| Vanilla JavaScript (no framework) | `static/catalogo/js/app.js`, `js/data.js` |
| Media queries | `css/styles.css`: header stacks under 576 px; product grid uses 1 / 2 / 3 / 4 columns from 0 / 576 / 992 / 1400 px |
| Bootstrap | `index.html` (Bootstrap 5.3.3 CDN): navbar, form control, badges, alerts, list group, buttons |
| CSS Grid | `.products-grid` (cards) and `.details` (label/value pairs inside each card) |
| Flexbox | `.header-flex` (header), `.product-card` (card column), suggestion items |
| Tailwind CSS | `index.html` loads Tailwind 3.4.17 (CDN) with prefix `tw-` and preflight disabled, so it does not clash with Bootstrap; used for text and spacing utilities in the cards |
| Instant, predictive search | `app.js`: runs on every keystroke with no delay (`input` event), accent-insensitive, all words must match, matches are highlighted, and a suggestions list (top 5) supports ↑ ↓ Enter Esc |
| Cards with description and details (text only) | `card()` in `app.js`: name, code, description, price, stock status, date added, product number |

## Technology stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.1 |
| Web layer | Spring Web MVC: `@Controller` (Thymeleaf) and `@RestController` (JSON) |
| Persistence | Spring Data JPA + Hibernate (`ddl-auto=update`) |
| Database | PostgreSQL |
| Templates | Thymeleaf (CRUD pages use Bootstrap 4.1.3 from the boilerplate) |
| Build tool | Maven (Maven Wrapper included): `./mvnw spring-boot:run` |
| Catalog | Vanilla JavaScript, Bootstrap 5.3.3 (CDN), Tailwind CSS 3.4.17 (CDN, prefix `tw-`) |
| Fonts | IBM Plex Sans, Space Grotesk (Google Fonts) |
| IDE | NetBeans (`nbactions.xml` included) |

## Project structure

```
ProgramacionWebPage1/
├── pom.xml
├── products_seed.sql                          ← 20 sample products (one INSERT)
├── README.md
└── src/main/
    ├── java/uce/edu/ProgramacionWeb/
    │   ├── ProgramacionWebApplication.java    ← Spring Boot entry point
    │   ├── Controller/
    │   │   ├── ProductoController.java        ← Thymeleaf CRUD routes
    │   │   └── ProductoRestController.java    ← REST API (new)
    │   ├── Model/Producto.java                ← JPA entity → table "producto"
    │   ├── Repository/ProductoRepository.java ← JpaRepository
    │   └── Service/ProductoService.java       ← business logic
    └── resources/
        ├── application.properties
        ├── templates/                         ← Thymeleaf pages (CRUD)
        │   ├── index.html                     ← main menu (with the "Catalog" button)
        │   ├── listar_productos.html
        │   ├── nuevo_producto.html
        │   └── actualizar_producto.html
        └── static/catalogo/                   ← instant-search catalog (new)
            ├── index.html
            ├── css/styles.css
            └── js/
                ├── app.js                     ← fetch, search, suggestions, rendering
                └── data.js                    ← 12 demo products (fallback)
```

## What was added on top of the boilerplate

### 1. `ProductoRestController.java` — REST API

A `@RestController` mapped to `/api/productos`. It reuses the existing `ProductoService`, so no business logic was duplicated.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/productos` | All products as a JSON array |
| `GET` | `/api/productos/{id}` | One product; `404` if it does not exist |
| `POST` | `/api/productos` | Creates a product (JSON body); any `id` sent is ignored |
| `PUT` | `/api/productos/{id}` | Updates the product with that id |
| `DELETE` | `/api/productos/{id}` | Deletes the product; returns `204 No Content` |

The catalog and the REST controller are served by the same Spring Boot server, so a relative URL (`/api/productos`) is used and CORS is not needed.

### 2. `templates/index.html` — "Catalog" button

A green button in the Thymeleaf menu links to the catalog:
```html
<a th:href="@{/catalogo/index.html}" class="btn btn-success btn-sm mb-3">Catalog (search)</a>
```

### 3. `static/catalogo/` — the catalog

- **On load** it calls `GET /api/productos` (`fetch` + `async/await`, 3-second timeout).
- **Fallback:** if the call fails or returns an error status, it shows the 12 products from `data.js` and a warning. (If the API answers with an empty list, the catalog is empty too: insert products first or run `products_seed.sql`.)
- **Instant search:** filters name, code and description while the user types, ignoring accents; every typed word must match.
- **Suggestions:** a `role="listbox"` dropdown with the top 5 name matches, usable with the keyboard or the mouse.
- **Cards:** name, code, description, price, stock status (*In stock*, *Low stock* for 5 units or fewer, *Out of stock* for 0), date added and product number.
- **Security:** every value is escaped with `escapeHtml()` before it is inserted with `innerHTML`.
- **Accessibility:** `role="combobox"`, `aria-expanded`, `aria-activedescendant`, `aria-live` on the result counter.
- **Navigation:** a "Back to menu" button returns to `/`.

### 4. `products_seed.sql`

A single `INSERT` statement with 20 products. Run it in pgAdmin after the first start, once Hibernate has created the table.

## How to run

### Prerequisites
- JDK 17 or newer
- PostgreSQL running on `localhost:5432`
- NetBeans (or any IDE / terminal with Maven)

### 1. Create the database
```sql
CREATE DATABASE "ProgramacionWebPage1";
```

### 2. Check credentials
`src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ProgramacionWebPage1
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
In pgAdmin, open **Query Tool** on the `ProgramacionWebPage1` database and run `products_seed.sql`.

### 5. Open it
| URL | What you see |
|-----|--------------|
| http://localhost:8080 | Main menu (Thymeleaf) |
| http://localhost:8080/listarProductos | Product list (CRUD) |
| http://localhost:8080/catalogo/index.html | Instant-search catalog |
| http://localhost:8080/api/productos | REST API (JSON) |

## Quick API test (Postman)

```
GET    http://localhost:8080/api/productos
GET    http://localhost:8080/api/productos/1

POST   http://localhost:8080/api/productos
Content-Type: application/json
{
  "codigo": "TEST-001",
  "nomProducto": "Test Product",
  "descripcion": "A test product",
  "precio": 9.99,
  "stock": 5,
  "fechIngreso": "2026-01-01"
}

PUT    http://localhost:8080/api/productos/1      (same JSON body)
DELETE http://localhost:8080/api/productos/1
```

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

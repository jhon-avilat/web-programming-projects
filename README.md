# Web Programming — Spring Boot Projects

This repository contains two independent Spring Boot projects. Both start from the same course boilerplate (layered Spring Boot + Thymeleaf + PostgreSQL CRUD) and each one adds a different front-end page.

## Projects

| Folder | What it adds | Port | Database | Extra page |
|--------|--------------|------|----------|-----------|
| [`ProgramacionWebPage1/`](./ProgramacionWebPage1/) | REST API + instant-search product catalog | `8080` | `ProgramacionWebPage1` | `/catalogo/index.html` |
| [`ProgramacionWebPage2/`](./ProgramacionWebPage2/) | Rick and Morty page (third-party API) | `8081` | `ProgramacionWebPage2` | `/page2/index.html` |

Each project is a **self-contained Maven project** with its own database, its own port and its own README. They can run **at the same time** because they use different ports (8080 and 8081) and different databases.

## What both projects inherit from the boilerplate

- Spring Boot application with the package `uce.edu.ProgramacionWeb` (unchanged).
- Layered structure: `Controller` → `Service` → `Repository` → `Model`.
- PostgreSQL through Spring Data JPA / Hibernate (`ddl-auto=update`: the table is created automatically).
- Thymeleaf server-side pages for a full CRUD (list, insert, edit, delete).
- A `Producto` entity (`numProducto`, `codigo`, `nomProducto`, `descripcion`, `precio`, `stock`, `fechIngreso`) mapped to the `producto` table. It follows the same pattern as the course boilerplate entity.

## What each project adds

**ProgramacionWebPage1** — a REST API (`/api/productos`, full CRUD in JSON) and a catalog built with vanilla JavaScript: instant search, autocomplete suggestions and text-only product cards. If the API cannot be reached, the catalog shows 12 demo products.

**ProgramacionWebPage2** — a vanilla JavaScript page that consumes the public [Rick and Morty API](https://rickandmortyapi.com/): responsive character cards with images, pagination, a search with a 4-second delay and a modal with the full details of each character.

## Quick start (for each project)

1. Make sure **PostgreSQL** is running on `localhost:5432`.
2. Create the database of the project (see its README).
3. Check `src/main/resources/application.properties`. The default credentials, `postgres / 1234`, are placeholders for local development: change them to match your PostgreSQL installation.
4. Open the project folder in **NetBeans** and click **Run**, or run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
5. Open the project URL:
   - Page 1 → http://localhost:8080
   - Page 2 → http://localhost:8081

## Repository structure

```
web-programming-projects/
├── README.md
├── .gitignore
├── ProgramacionWebPage1/
│   ├── README.md
│   ├── pom.xml
│   ├── products_seed.sql
│   └── src/
└── ProgramacionWebPage2/
    ├── README.md
    ├── pom.xml
    ├── products_seed.sql
    └── src/
```

## Technology overview

| Layer | Technology |
|-------|-----------|
| Language | Java 17 (runs on newer JDKs too) |
| Framework | Spring Boot 4.0.1 (Spring Web MVC) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Server-side templates | Thymeleaf (with Bootstrap 4.1.3 from the boilerplate) |
| Static pages | Vanilla JavaScript, Bootstrap 5.3.3 (CDN), Tailwind CSS 3.4.17 (CDN, `tw-` prefix), CSS Grid, Flexbox, media queries |
| Build tool | Maven (Maven Wrapper included) |
| IDE | NetBeans |
| Third-party API | Rick and Morty API (Page 2 only) |

# Web Programming — Course Projects

Monorepo with all the projects of the **Web Programming** course (UCE).
Every assignment lives in its own folder under [`assignments/`](./assignments/) and can be built, tested and deployed on its own.

## Assignments

| # | Folder | What it is | Stack | Status |
|---|--------|-----------|-------|--------|
| 01 | [`assignments/01-product-catalog`](./assignments/01-product-catalog/) | Product CRUD + REST API + instant-search catalog (Page 1) and Rick and Morty page (Page 2) | Spring Boot 4, Thymeleaf, PostgreSQL, Docker | Active |

## Repository layout

```
web-programming-projects/
├── README.md                 ← this index
├── assignments/
│   └── 01-product-catalog/   ← one folder per assignment
└── .gitignore
```

## Adding a new assignment

1. Create the Jira issue first.
2. Create a folder `assignments/NN-short-name/` with its own README.
3. Add a row to the table above.

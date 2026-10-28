# Web Programming — Course Projects

[![Assignment 01 · CI](https://github.com/jhon-avilat/web-programming-projects/actions/workflows/assignment-01-ci.yml/badge.svg)](https://github.com/jhon-avilat/web-programming-projects/actions/workflows/assignment-01-ci.yml)

Monorepo with all the projects of the **Web Programming** course (UCE).
Every assignment lives in its own folder under [`assignments/`](./assignments/) and can be built, tested and deployed on its own.

## Assignments

| # | Folder | What it is | Stack | Status |
|---|--------|-----------|-------|--------|
| 01 | [`assignments/01-product-catalog`](./assignments/01-product-catalog/) | Product CRUD + REST API + instant-search catalog (Page 1) and Rick and Morty page (Page 2) | Spring Boot 4, Thymeleaf, PostgreSQL, Docker | [![CI](https://github.com/jhon-avilat/web-programming-projects/actions/workflows/assignment-01-ci.yml/badge.svg)](https://github.com/jhon-avilat/web-programming-projects/actions/workflows/assignment-01-ci.yml) |

## Repository layout

```
web-programming-projects/
├── README.md                 ← this index
├── .github/workflows/        ← one CI workflow per assignment
├── assignments/
│   └── 01-product-catalog/   ← one folder per assignment
├── docs/deployment.md        ← how everything is deployed to AWS EC2
└── .gitignore
```

## Adding a new assignment

1. Create the Jira issue first.
2. Create a folder `assignments/NN-short-name/` with its own README.
3. Add a row to the table above.
4. Copy `.github/workflows/assignment-01-ci.yml`, rename it and change the paths.

## Deployment

Everything runs on one AWS EC2 instance with Docker Compose, deployed from GitHub Actions on demand.
See [docs/deployment.md](./docs/deployment.md).

## How we work

Jira issue → branch → small commits → pull request → CI → merge. See [CONTRIBUTING.md](./CONTRIBUTING.md).

# How we work in this repo

This is how every change gets into `main`. Even small fixes follow it, so the
history, the pull requests and the Jira board always tell the same story.

## 1. Start in Jira

Every change starts as an issue in the Jira project **KAN**
(https://jhonavilat105.atlassian.net). Write the acceptance criteria before
writing code, then move the issue to **In Progress**.

| Issue type | Use it for |
|------------|-----------|
| Epic  | A big goal that groups several issues |
| Story | Something a user can see or do |
| Task  | Technical work (config, refactor, CI…) |
| Bug   | Something that is broken |

## 2. Create a branch

Always branch off `main` (or off the branch you depend on) and put the Jira key in the name:

```
feature/KAN-15-page2-rest-api
fix/KAN-10-delete-with-post
chore/KAN-12-ci-workflow
```

Never commit directly to `main`.

## 3. Commit in small steps

One commit = one idea. The subject line follows
[Conventional Commits](https://www.conventionalcommits.org/) and ends with the Jira key:

```
feat(page2): add favorites endpoints (KAN-15)

Explain in plain words what changed and why, so it still makes sense
when you come back to it in a few months.
```

Common types: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`, `style`.

## 4. Open a pull request

- Push the branch and open a PR to `main` using the PR template.
- Link the Jira issue and move it to **In Review**.
- CI (GitHub Actions) must be green before merging.

## 5. Merge and close

- Merge with a **merge commit** so every small commit stays visible in the history.
- Delete the branch.
- Move the Jira issue to **Done**, or back to **To Do** with a comment if it was rejected.

## Monorepo layout

Each assignment lives in `assignments/NN-short-name/` and has its own README,
its own build and its own CI workflow. Changes to one assignment should not
touch another one.

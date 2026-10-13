# Git collaboration evidence (Weeks 5–6)

## Pull request

- PR: https://github.com/ash01825/devops/pull/1
  ("Viva polish: security hardening, role-gated UI, truthful pipeline, evidence")
- Branch: `feature/viva-polish` → `master` (naming follows
  `docs/04-git-branching-policy.md`: `feature/*`).
- Review: author review + Q&A comment on the PR (why the Selenium gate still
  runs under plain `mvn verify`), then merge commit `b3ed82f` (GitHub merge,
  `--no-ff` equivalent). Single-contributor repo: the PR thread is the review
  record (`gh pr view 1 --comments`).
- Verify: `gh pr view 1 --comments` shows review + discussion;
  `git log --graph` shows the merge; the branch was deleted after merge
  (remote ref removed — PR page is the permanent record).

## Earlier merge + conflict

- `cb60feb` — `feature/release-polish` merged into `master` with a genuine
  `README.md` conflict, resolved to keep both wordings, then tagged `v1.0.0`.

## Tags

- `v1.0.0` (annotated) — MVP release baseline; notes in `CHANGELOG.md`.

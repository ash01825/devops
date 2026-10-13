# Git Workflow

**Applies to:** Water Quality Sampling Portal
**Default branch:** `master` (small, direct commits land here)

## How we work

Day-to-day fixes and docs go straight to `master` in small, meaningful commits.
Any review-gated change (features, security, pipeline edits) goes through a
short-lived branch + pull request so somebody else looks before it merges:

1. Create a branch from `master`.
2. Commit with a meaningful message.
3. Push the branch and open a pull request.
4. Address review comments, then merge (merge commit, `--no-ff`).
5. Delete the branch after merge.

Evidence: `feature/release-polish` → conflict → resolution merge `cb60feb`;
`feature/viva-polish` → PR #N (see `docs/evidence/git-pr-simulation.md`).

## Branch naming rules

| Prefix | Use for | Example |
|--------|---------|---------|
| `feature/` | New stories / UI / endpoints | `feature/dashboard-charts` |
| `fix/` | Bug fixes | `fix/workflow-role-guard` |
| `docs/` | Docs only | `docs/selenium-test-plan` |
| `chore/` | Tooling, CI, chores | `chore/bump-base-image` |
| `release/` | Release prep | `release/v1.1.0` |

Rules: lowercase, slashes separate scope, short hyphenated summary, one
concern per branch, delete after merge.

## Commit message convention

```
<type>: <short summary>
```

Types: `feat`, `fix`, `docs`, `chore`, `test`, `refactor`, `ci`, `build`.

Examples:
- `feat: add sample create and view`
- `test: cover sample workflow journey`
- `docs: add data model`
- `ci: add Jenkins pipeline`

## Tags

- Tags mark release points.
- Format: `v<major>.<minor>.<patch>` (e.g., `v1.0.0`).
- Releases are annotated tags (`git tag -a`) with notes in `CHANGELOG.md`.

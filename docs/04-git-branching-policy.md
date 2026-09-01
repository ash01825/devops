# Git Workflow

**Applies to:** Water Quality Sampling Portal
**Branch:** `master` (single branch, direct commits)

## Keep it simple

We work on one branch — `master`. No feature branches, no `develop`, no pull requests.

1. Make changes locally.
2. Commit with a meaningful message.
3. Push to the remote.

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

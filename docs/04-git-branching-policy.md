# Git Branching Policy

**Applies to:** Water Quality Sampling Portal
**Default branch:** `main` (release-ready) · **Integration branch:** `develop` (working)

## Branch naming rules

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/<short-description>` | `feature/sample-crud` |
| Bug fix | `bugfix/<short-description>` | `bugfix/sample-search-null` |
| Hotfix | `hotfix/<short-description>` | `hotfix/security-patch` |
| Release | `release/v<major>.<minor>.<patch>` | `release/v1.0.0` |

Rules:
- Use lowercase kebab-case.
- Keep the description short and descriptive.
- Never commit directly to `main`; all changes flow through `develop` via pull request.

## Workflow

```
main            ← tagged, release-ready only
  ▲  (merge)
develop         ← integration branch, always deployable
  ▲  (pull request + review)
feature/xyz     ← short-lived feature branch
```

1. Create a feature branch from `develop`.
2. Commit small, meaningful changes on the branch.
3. Push and open a pull request targeting `develop`.
4. At least one review approves; builds/tests pass.
5. Merge into `develop` (done by the repository owner).

## Commit message convention

```
<type>(<scope>): <short summary>
```

Types: `feat`, `fix`, `docs`, `chore`, `test`, `refactor`, `ci`, `build`.

Examples:
- `feat(sample): add create and view endpoints`
- `test(selenium): cover sample workflow journey`
- `docs(architecture): add data model`
- `ci(jenkins): add pipeline as code`

## Tags

- Tags are created only on `main` for release points.
- Format: `v<major>.<minor>.<patch>` (e.g., `v1.0.0`).

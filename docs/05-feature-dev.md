# Week 5 — Feature Development (Login + Create/View Samples)

**Sprint theme:** Feature 1 — US-1.1 (login), US-2.1 (create sample), US-2.2 (view detail).
**Branch:** `master` (direct commits, per `docs/04-git-branching-policy.md`).

## What was built

- **Login/logout** — `LoginController` (`GET /login` → `login.html`), form login in
  `SecurityConfig` (success → `/dashboard`, logout → `/login?logout`), BCrypt passwords.
- **Create sample** — `SampleViewController.newForm` (`GET /samples/new` → `samples/form`),
  `SampleFormController.create` (`POST /samples` → redirect to `/samples/{id}`).
  New samples start in `DRAFT`; duplicate `sampleId` shows "Sample ID already exists".
- **View sample** — `SampleViewController.view` (`GET /samples/{id}` → `samples/view`)
  with measurements plus status history from `SampleService.historyFor`.
- **Seed data** — `DataInitializer` (idempotent: skips when users exist) creates users
  `admin` / `collector` / `analyst` / `reviewer` and stations `ST-01`–`ST-04`
  plus samples `WQ-0001`–`WQ-0006`.

## Files changed

| Area | Files |
|------|-------|
| Controllers | `SampleViewController.java`, `SampleFormController.java`, `LoginController.java` |
| Security | `config/SecurityConfig.java` (`formLogin`, `/stations/**` → `ADMIN`) |
| Seed | `config/DataInitializer.java` |
| Templates | `templates/login.html`, `templates/samples/form.html`, `templates/samples/view.html`, `templates/samples/list.html`, `templates/dashboard.html` |
| Domain/service | `domain/Sample.java`, `domain/Station.java`, `service/SampleService.java`, `service/StationService.java` |

## How to demo (login as collector, create WQ-0101)

1. Run: `cd water-quality-portal && mvn spring-boot:run`
2. Open `http://localhost:8080/water-quality-portal/login`
3. Log in as `collector` / `collector123` → lands on `/dashboard`.
4. Click **New Sample** (or open `/samples/new`), fill in:
   - Sample ID: `WQ-0101`, Station: `ST-01`, collectedAt: yesterday,
   - Collector: your name, pH `7.2`, notes `Week 5 demo`.
5. Save → redirected to `/samples/{id}`, badge shows `DRAFT`, history has 1 entry
   ("Sample created"), flash message `Sample WQ-0101 created.`

## Definition of Done check

- `mvn clean verify` green; manual click-through per US-1.1/US-2.1/US-2.2 AC;
  committed to `master` with `feat:` message.
- Remaining stories (edit, search, workflow, dashboard counts) move to Week 6.

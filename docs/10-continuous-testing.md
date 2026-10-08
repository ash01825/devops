# Week 10 — Continuous Testing (Quality Gate)

**Goal (US-5.2):** failed tests block deployment — bad code never reaches the target.

## How the gate works

In `jenkins/Jenkinsfile`, the **Selenium Quality Gate** stage sits *before*
Archive/Docker Build/Deploy. Declarative pipelines abort on first stage failure,
so:

- `Unit Test` red → `junit` publishes the failure, Archive/Docker/Deploy never run.
- `Selenium Quality Gate` red → same; `post.failure` prints
  "Pipeline FAILED — deployment was NOT performed".
- `SKIP_SELENIUM=true` bypasses the gate — only for browser-less agents, never to
  hide a red suite.

Failsafe + surefire XML are published in the stage's `post.always`, so the
**Test Result** page shows exactly which journey failed.

## How a failure looks

- Jenkins: build red at `Selenium Quality Gate`; console shows the failing journey
  (e.g. `journey2_createSampleRecord`) and Deploy stage is absent.
- Workspace: `water-quality-portal/target/selenium-failures/<journey>-FAIL-<ts>.png`
  plus matching `.html` page source (saved by `screenshot(name)`).
- Local repro: `mvn -B verify -Dtest=WaterQualitySeleniumTest` fails the same way.

## Defect–fix workflow (red → fix → green)

1. **Red:** gate fails, evidence (screenshot/HTML/console) attached to the defect.
2. **Fix:** minimal code change on `master`, committed with `fix:` message.
3. **Green:** pipeline re-runs full suite; Deploy proceeds only when all green.

## Real example: station-converter binding bug (found by journey 2)

- **Symptom:** journey 2 filled the form but stayed on `samples/form` — no redirect,
  no `DRAFT` badge. `diagnoseForm()` + `typeMismatch` pointed at the station field:
  the form posts a station *id string*, but nothing converted it to a `Station` entity.
- **Fix:** `config/StringToStationConverter.java` (`Converter<String, Station>`,
  `@Component`) — blank → `null`, otherwise `StationRepository.findById`, throwing
  `IllegalArgumentException("Unknown station: …")` for bad ids. Spring picks it up
  automatically via the conversion service.
- **Green:** journey 2 passes — form submits, detail shows `DRAFT` + new sample ID.

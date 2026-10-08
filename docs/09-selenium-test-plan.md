# Week 9 — Selenium Test Plan

**Suite:** `water-quality-portal/src/test/java/com/waterquality/portal/WaterQualitySeleniumTest.java`
(`@SpringBootTest` RANDOM_PORT, headless Chrome via WebDriverManager).
Unit/integration companions: `SampleServiceTest`, `WebSecurityTest`.

## Journeys (5)

| ID | Test method | Flow | Key assertions |
|----|-------------|------|----------------|
| j1 | `journey1_loginShowsDashboard` | login as `admin` → dashboard | `#summaryHeading` mentions samples; `#statusCards .stat` non-empty |
| j2 | `journey2_createSampleRecord` | login as `collector` → `/samples/new` → submit | lands on detail, `#statusBadge` = `DRAFT`, page contains new ID |
| j3 | `journey3_searchSamples` | search `q=WQ-0001`, then `q=ZZZ-NO-SUCH-SAMPLE` | exactly 1 row with `WQ-0001`; empty set shows `#noResults` |
| j4 | `journey4_collectorSubmitsDraft` | collector opens DRAFT → `btn-SUBMITTED` | badge becomes `SUBMITTED`; `#historyTable` ≥ 1 row |
| j5 | `journey5_dashboardCountsAndHistory` | login as `reviewer` → `/dashboard` | `#stationTable` and `#recentTable` non-empty |

Each journey runs inside `run(name, journey)` — on failure a timestamped
screenshot + page HTML is saved to `target/selenium-failures/`.

## Test data

Seeded by `DataInitializer`: users `admin/admin123`, `collector/collector123`,
`analyst/analyst123`, `reviewer/reviewer123`; stations `ST-01`..`ST-04`;
samples `WQ-0001`..`WQ-0006` (mixed statuses). Journeys create unique IDs
(`WQ-SEL-<ts>`, `WQ-DRF-<ts>`) to avoid `sampleId` collisions.

## Browser & robustness helpers

- Headless Chrome: `--headless=new --no-sandbox --disable-dev-shm-usage`, 1280×900;
  driver binary from `WebDriverManager.chromedriver().setup()`.
- `type(driver, id, value)` — click+clear+sendKeys with JS fallback (headless Chrome
  drops keystrokes); `setDateTime` sets `datetime-local` via JS + input/change events.
- Submits use `form.requestSubmit(saveBtn)`; `diagnoseForm()` prints HTML5 validity
  per control when a submit is blocked.

## Run commands

```bash
cd water-quality-portal
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
mvn -B verify                                # full suite (unit + Selenium)
mvn -B verify -Dtest=WaterQualitySeleniumTest # Selenium only
```

Requires Google Chrome installed (WebDriverManager needs a browser binary).

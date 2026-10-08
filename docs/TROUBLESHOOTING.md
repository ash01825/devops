# Troubleshooting

## JAVA_HOME picks Java 26 (or wrong version)

Symptom: `mvn` fails with release/class-file errors.
Fix: force Temurin 21 per session (the Jenkinsfile does the same):

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
java -version   # must show 21
```

## Port 8080 busy

Symptom: `port already allocated` / bind error.
Fix: find and stop the holder (or remap):

```bash
lsof -i :8080
kill <PID>   # or: docker stop wq-portal-app
# alternative: -p 8081:8080 and use :8081
```

## Selenium: Chrome missing

Symptom: `WebDriverManager` / `ChromeDriver` fails to start.
Fix: install Google Chrome (stable) on the machine running
`WaterQualitySeleniumTest`; headless flags are already set. Browser-less CI
agents may use `SKIP_SELENIUM=true` temporarily — never to hide red tests.

## sendKeys flakiness (values don't stick)

Symptom: fields empty after typing in headless Chrome.
Fix: already handled — `type()` helper (click+clear+sendKeys, JS fallback) and
`setDateTime` (JS value + input/change events); submits use `requestSubmit`.
If adding fields, reuse these helpers and keep element `id`s stable.

## Form stays on page (no redirect, `typeMismatch`)

Symptom: submit returns `samples/form` with errors.
Fix: check `diagnoseForm()` output and the `error` banner. Station id binding needs
`config/StringToStationConverter`; unknown station → "Unknown station: …".
Validation failures re-render the form with messages — fill required fields
(station, collectedAt, collector, sampleId).

## Docker daemon off

Symptom: `Cannot connect to the Docker daemon`.
Fix: start Docker Desktop (macOS) then `docker info`. Jenkins-Docker and compose
deploy both need the daemon.

## Ansible not installed

Symptom: `ansible-playbook: command not found`.
Fix: use the `os` conda env:

```bash
conda activate os
pip install ansible   # if the env lacks it
ansible-galaxy collection install -r ansible/requirements.yml
```

## H2 console path

Dev profile uses in-memory H2; console at `/h2-console` (permitted in
`SecurityConfig`, frame `sameOrigin`). JDBC URL/credentials come from
`application.yml` — never use H2 data as prod evidence.

## sampleId duplicates

Symptom: "Sample ID already exists. Choose a unique ID."
Fix: `sampleId` is unique and immutable (`SampleService.create` rejects dupes;
seed ids `WQ-0001`–`WQ-0006` exist). Use a fresh id (e.g. `WQ-0101`); Selenium
journeys use `WQ-SEL-<ts>` / `WQ-DRF-<ts>` for this reason.

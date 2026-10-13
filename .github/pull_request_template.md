# Pull request template (concise)

## What changed
- …

## Why
- … (link story ID, e.g. US-2.4, or the defect)

## How to verify
- [ ] `mvn -B verify` green (or state what was run)
- [ ] Manual click-path tested (list exact pages/buttons)
- [ ] Docs updated (`docs/…`) if behaviour changed

## Pipeline impact
- [ ] No pipeline change
- [ ] Jenkinsfile / Docker / Ansible changed → rehearsal log attached

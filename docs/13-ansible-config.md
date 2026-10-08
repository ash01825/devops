# Week 13 — Ansible Configuration Management

**Sources:** `ansible/inventory.ini`, `ansible/playbook.yml`,
`ansible/requirements.yml`, `ansible/rollback.yml`. Helper env: `os` conda env
(`conda activate os`, or one-shot `conda run -n os …`).

## Prerequisites

| Category | Requirement | Check / setup |
|----------|-------------|---------------|
| Packages | Python 3.10+, ansible-core 2.15+, curl, Docker 24+ | `conda run -n os ansible --version`; `brew install curl docker` |
| Collections | `community.docker`, `ansible.posix` | `ansible-galaxy collection install -r ansible/requirements.yml` |
| User | Current local user (no new users for local staging) | `whoami` (playbook records it) |
| Folders | `/opt/wq-portal` (deploy), `./postgres_data` (db helper) | Playbook `file: state=directory` |
| Files | `docker/docker-compose.yml` → `/opt/wq-portal/docker-compose.yml` | Playbook `copy` task |
| Ports | `8080` (app), `5433` (postgres host map) free | `lsof -i :8080 -i :5433` |
| Services | `db` + `app` via compose; Docker daemon running | `docker info` |

## inventory.ini

```ini
[portal]
localhost ansible_connection=local

[portal:vars]
app_version=1.0.0
app_port=8080
db_name=waterquality
db_user=wquser
db_password=wqpass
app_context=water-quality-portal
app_url=http://localhost:8080/water-quality-portal
```

## playbook.yml task summary

1. Doc note (curl + docker via Homebrew) → `curl --version` / `docker --version`
   checks + `assert` both exist.
2. `whoami` recorded (local user, no service user created).
3. `file state=directory` for `/opt/wq-portal` and `postgres_data/`.
4. `copy` compose file to `/opt/wq-portal/docker-compose.yml` (notifies restart handler).
5. `docker_image pull postgres:16-alpine`; `docker_compose_v2 state=present, pull=missing`.
6. `wait_for 127.0.0.1:8080` then `uri GET .../actuator/health` (200, 12×10 s retry).

## requirements.yml collections

`community.docker (>=3.4.0)` for `docker_image`/`docker_compose_v2`/`docker_container`;
`ansible.posix` for platform modules. Install once (see table above).

## First-run command

```bash
conda activate os
ansible-galaxy collection install -r ansible/requirements.yml   # once
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml  # expect changed=5-ish
```

Ends with `PLAY RECAP ... failed=0` and the health-check `ok`.

> Note: run Ansible with the conda `os` interpreter so the `requests`/`docker`
> libraries resolve, and override `deploy_dir` when `/opt` is not writable:
> `conda run -n os ansible-playbook -i ansible/inventory.ini ansible/playbook.yml \
> -e ansible_python_interpreter=$(conda run -n os which python) -e deploy_dir=$HOME/wq-portal-deploy`

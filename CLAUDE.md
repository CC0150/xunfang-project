# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XunFang (讯方) v3.6.6 — a full-stack enterprise management platform built on the open-source **RuoYi-Cloud** microservices framework. The backend is Java Spring Cloud Alibaba; the frontend is Vue 3 + Vite + Element Plus. They communicate via REST APIs with Bearer token auth through the gateway.

```
Browser (port 80) → Nginx / Vue Dev Server
                      ↓
              Gateway (8080) ← Nacos registry/config (8848)
                      ↓
         ┌────────────┼──────────────┬──────────────┐
         ↓            ↓              ↓              ↓
       Auth         System          Gen            Job
      (9200)        (9201)         (9202)         (9203)
                      ↓
              ┌───────┴───────┐
              ↓               ↓
          MySQL (3306)    Redis (6379)
```

## Repository Structure

| Directory | Purpose |
|-----------|---------|
| `xunfang-ui-cb-53/` | Vue 3 frontend SPA (see `xunfang-ui-cb-53/CLAUDE.md` for detailed frontend docs) |
| `xunfang-gateway/` | Spring Cloud Gateway — API routing, Sentinel rate limiting |
| `xunfang-auth/` | Auth center — login, token issuance/validation, user info |
| `xunfang-api/` | Feign interface definitions for inter-service calls (currently `xunfang-api-system`) |
| `xunfang-common/` | Shared libraries (core, security, redis, datasource, datascope, log, swagger, sensitive, seata) |
| `xunfang-modules/` | Business services: `xunfang-system`, `xunfang-gen`, `xunfang-job`, `xunfang-file` |
| `xunfang-visual/` | Spring Boot Admin monitor (`xunfang-monitor`, port 9100) |
| `docker/` | Docker Compose deployment (12 services) |
| `sql/` | Database init scripts |
| `bin/` | Windows batch scripts to run individual services |

## Essential Commands

### Backend (Java / Maven)

```bash
# Build all modules (skip tests — there are none)
mvn clean package -Dmaven.test.skip=true

# Build a single module
mvn clean package -Dmaven.test.skip=true -pl xunfang-gateway -am

# Run a service (after building)
java -Dfile.encoding=utf-8 -jar xunfang-gateway/target/xunfang-gateway.jar
java -Dfile.encoding=utf-8 -jar xunfang-auth/target/xunfang-auth.jar
java -Dfile.encoding=utf-8 -jar xunfang-modules/xunfang-system/target/xunfang-system.jar
java -Dfile.encoding=utf-8 -jar xunfang-modules/xunfang-gen/target/xunfang-gen.jar
java -Dfile.encoding=utf-8 -jar xunfang-modules/xunfang-job/target/xunfang-job.jar
java -Dfile.encoding=utf-8 -jar xunfang-modules/xunfang-file/target/xunfang-file.jar
java -Dfile.encoding=utf-8 -jar xunfang-visual/xunfang-monitor/target/xunfang-monitor.jar
```

Alternatively, use the `.bat` scripts in `bin/` (Windows only): `run-gateway.bat`, `run-auth.bat`, `run-modules-system.bat`, `run-modules-gen.bat`, `run-modules-job.bat`, `run-modules-file.bat`, `run-monitor.bat`.

### Frontend (Vue 3 / Vite)

```bash
cd xunfang-ui-cb-53
npm run dev          # Dev server on port 80, proxies /dev-api → localhost:8080
npm run build:prod   # Production build
npm run build:stage  # Staging build
npm run preview      # Preview production build
```

### Docker

```bash
cd docker
docker-compose up -d    # Start all 12 services (nacos, mysql, redis, nginx, gateway, auth, 4 modules, monitor)
```

### Database

```bash
# Initialize: run the SQL files in sql/ against MySQL 5.7
# Order: ry_seata_20210128.sql → quartz.sql → ry_20250523.sql → ry_config_20250224.sql
mysql -u root -p < sql/ry_seata_20210128.sql
mysql -u root -p < sql/quartz.sql
mysql -u root -p < sql/ry_20250523.sql
mysql -u root -p ry-cloud < sql/ry_config_20250224.sql   # Nacos config (imports into nacos db)
```

## Backend Architecture

### Tech Stack

- **Java 1.8**, **Spring Boot 2.7.18**, **Spring Cloud 2021.0.9**, **Spring Cloud Alibaba 2021.0.6.1**
- **Nacos** — service registry + config center (standalone mode, `127.0.0.1:8848`)
- **Sentinel** — rate limiting / circuit breaking (dashboard at `127.0.0.1:8718`)
- **Spring Cloud Gateway** — single entry point (port 8080), routes to downstream services
- **Spring Security + JWT (jjwt 0.9.1)** — authentication; tokens stored in Redis
- **MyBatis + PageHelper** — ORM + pagination; mapper XML files in `src/main/resources/mapper/`
- **Druid 1.2.23** — connection pooling with dynamic multi-datasource support
- **Springdoc OpenAPI 1.6.9** — auto-generates API docs from controller annotations
- **Seata** — distributed transaction support
- **MinIO 8.2.2 / FastDFS** — file storage (configurable)
- **Quartz** — job scheduling
- **Apache Velocity 2.3** — code generation templates
- **Fastjson2 2.0.57** — JSON serialization

### Configuration Model

Each service has a `bootstrap.yml` that defines:
- Server port
- Nacos discovery + config server address
- Sentinel dashboard + datasource
- Spring profile (`dev` by default)

Nacos serves **shared config** (`application-dev.yml`) and per-service config files. Runtime configuration (DB connections, Redis, secrets) is pulled from Nacos, not hardcoded.

### Common Module Details

| Module | Provides |
|--------|----------|
| `xunfang-common-core` | `R<T>` unified response, `@Excel`/`@Excels` annotations, exception hierarchy, HTTP status codes, `SecurityContextHolder` |
| `xunfang-common-security` | Spring Security config, JWT token service, auth filters |
| `xunfang-common-redis` | Redis cache service with key prefixing |
| `xunfang-common-datasource` | Dynamic multi-datasource via `@DataSource` annotation + Druid |
| `xunfang-common-datascope` | Row-level data permission scoping (department-based) |
| `xunfang-common-log` | AOP-based operation logging (`@Log` annotation) |
| `xunfang-common-swagger` | Springdoc OpenAPI auto-configuration |
| `xunfang-common-sensitive` | Data masking/desensitization annotations |
| `xunfang-common-seata` | Seata distributed transaction auto-config |

### Inter-Service Communication

Services call each other via **Feign** interfaces defined in `xunfang-api/xunfang-api-system/`. Each Feign client has a **Sentinel fallback factory** for circuit breaking. Example pattern:

```
RemoteUserService (Feign) → xunfang-system
  └── RemoteUserServiceFallbackFactory (Sentinel fallback)
```

### Layered Architecture (per business module)

```
controller/  → REST endpoints, @Log for operation logging
service/     → Business logic (interface + impl pattern)
mapper/      → MyBatis interfaces
domain/      → Entity classes + VO (RouterVo, MetaVo, TreeSelect)
```

Controllers return `R<T>` (wraps HTTP status, message, data). Pagination uses PageHelper's `startPage()` before queries. Excel export uses `@Excel` annotation on entity fields + `ExcelUtil`.

### Auth Flow

1. Login: `POST /auth/login` → validates credentials → returns JWT token
2. Token stored as `Authorization: Bearer <token>` on all subsequent requests
3. Gateway validates token via Redis (checks it exists and isn't expired)
4. `SecurityContextHolder` (TL-based) holds current user context per request
5. Super admin role is `admin`, wildcard permission is `*:*:*`

### Build Structure

Root `pom.xml` declares 6 submodules. Maven wrapper uses Alibaba mirror (`maven.aliyun.com/repository/public`). Overridden dependency versions: Spring Framework 5.3.39, Tomcat 9.0.106, Logback 1.2.13.

## Frontend

See **`xunfang-ui-cb-53/CLAUDE.md`** for comprehensive frontend documentation covering:
- Full tech stack and build plugins
- Routing/permission flow (backend-driven dynamic routes)
- Pinia store modules
- Layout structure
- API layer (Axios interceptors, token handling)
- Global properties (`$auth`, `$tab`, `$cache`, `$modal`, `$download`)
- Global components and styling

## Development Prerequisites

To run the full system locally:

1. **Nacos** (standalone on 8848) — required by all backend services
2. **MySQL 5.7** (port 3306, database `ry-cloud`, root password `password`) — run SQL init scripts
3. **Redis** (port 6379) — token storage + caching
4. **Sentinel** dashboard (port 8718) — optional, for rate limiting visibility
5. Build all backend jars: `mvn clean package -Dmaven.test.skip=true`
6. Start services in order: Nacos → MySQL/Redis → Auth → Gateway → System → Gen → Job → File
7. Start frontend: `cd xunfang-ui-cb-53 && npm run dev`

Or use Docker: `cd docker && docker-compose up -d`

## Key URLs (dev)

| Component | URL |
|-----------|-----|
| Frontend | `http://localhost:80` |
| Gateway (API) | `http://localhost:8080` |
| Nacos console | `http://localhost:8848/nacos` |
| Sentinel dashboard | `http://localhost:8718` |
| Spring Boot Admin | `http://localhost:9100` |
| Swagger (per service) | `http://localhost:<port>/swagger-ui/index.html` |

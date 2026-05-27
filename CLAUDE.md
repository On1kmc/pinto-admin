# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 3.4.2 (Java 20, Maven) admin panel for managing Telegram bot users. Communicates with an external HTTPS backend API; renders UI via Thymeleaf server-side templates.

## Commands

```bash
# Run locally
./mvnw spring-boot:run

# Build
./mvnw clean package

# Run tests
./mvnw test
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

## Architecture

**MVC pattern:** two controllers call the external REST API, map JSON to DTOs, and pass them to Thymeleaf templates.

| Layer | Files |
|-------|-------|
| Controllers | `IndexController` (GET `/`, `/admins`), `UserController` (GET/POST `/users/*`) |
| DTOs | `UserDTO`, `PurchasesDTO` |
| Enums | `UserType` (USER/ADMIN), `Segment` (OFTEN/MEDIUM/SOMETIMES/LESS/NEVER) |
| Templates | `index.html` (paginated user list), `show-user.html` (detail/edit), `admins.html` |
| Config | `SecurityConfig` (in-memory HTTP Basic auth), `Config` (Thymeleaf Java8TimeDialect) |

**External API communication:** every controller method builds a fresh `CloseableHttpClient` using `TrustSelfSignedStrategy` + `NoopHostnameVerifier` (the backend uses a self-signed cert). The `Authorization` header value comes from `api.token` in `application.properties`.

**Key `application.properties` values:**
- `api.endpoint` — base URL of the backend (HTTPS with custom port)
- `api.token` — bearer token sent with every API request
- `server.port=80`

**Pagination:** `IndexController` fetches a full user list from the API and slices it in-memory; page size is 100.

**Security:** three hardcoded in-memory users are defined in `SecurityConfig` with BCrypt-encoded passwords. HTTP Basic auth protects all routes.

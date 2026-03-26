# Text Editor

A full-stack text editor application built as a portfolio project to explore modern web development practices — from backend design and database management to frontend development and cloud deployment.

**Live demo:** https://texteditorapi-production.up.railway.app

---

## Overview

The app lets users create and edit documents through a browser-based text editor. It supports keyboard-driven editing commands (move cursor, select text, delete, etc.) and persists all documents to a database.

The project covers the full development lifecycle: backend API design, frontend development, containerization, and deployment to a cloud platform.

---

## Tech Stack

### Frontend
- Angular (TypeScript)

### Backend
- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA / Hibernate

### Database
- PostgreSQL
- Flyway (schema migrations)

### Testing
- JUnit
- Testcontainers

### Infrastructure
- Docker (multi-stage build)
- Docker Compose
- Gradle
- Railway (cloud deployment)

---

## Architecture

The backend follows a layered architecture with clear separation of responsibilities:

- **API layer** (`api/`) — REST controllers, request/response models, global error handling
- **Service layer** (`service/`) — business logic, coordinates editing operations with persistence
- **Persistence layer** (`persistence/`) — JPA entities and Spring Data repositories
- **Editing model** (`TextBuffer`, `commands/`) — command pattern for text editing operations

The frontend is an Angular single-page application served as static files directly by Spring Boot. There is no separate web server.

### REST API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/documents` | Create a document |
| `GET` | `/api/documents` | List all documents |
| `GET` | `/api/documents/{id}` | Get a document |
| `POST` | `/api/documents/{id}/commands` | Apply an editing command |

---

## Running Locally

Requires Docker.

```bash
# Start the full stack (API + PostgreSQL)
docker compose up

# Run tests (requires Docker for Testcontainers)
./gradlew test
```

The app will be available at `http://localhost:8080`.

---

## Known Limitations

- **No authentication** — all users share the same documents. Authentication is a planned future addition.
- **Undo/redo** — not functional via the API. `TextBuffer` history is not persisted across requests; each command is applied to a fresh buffer reconstructed from the saved snapshot.

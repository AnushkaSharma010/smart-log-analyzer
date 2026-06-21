# Smart Log Analyzer

A production-style microservices platform that ingests application log files and generates structured insights — error summaries, exception counts, severity breakdown, and AI-assisted root cause suggestions.

## Tech Stack
Java 21 · Spring Boot 3 · Spring Cloud (Eureka, Gateway) · MySQL · MongoDB · OpenFeign · Resilience4j · React · Docker

## Status
🚧 Work in progress — built incrementally as part of structured backend interview preparation.

## Modules
- `discovery-server` — Eureka service registry (port 8761)
- `api-gateway` — Spring Cloud Gateway routing layer (port 8080)
- `log-service` — Log file upload and metadata management with MySQL (port 8081)
- `analysis-service` — Log parsing and analysis engine with MongoDB (port 8082)

## Progress
- [x] Phase 1: Eureka discovery server + service registration
- [x] Phase 2: API Gateway with path-based routing
- [x] Phase 3: Log Service — file upload, MySQL persistence, CRUD
- [ ] Phase 4: Analysis Service skeleton (MongoDB)
- [ ] Phase 5: OpenFeign integration
- [ ] Phase 6: Strategy Pattern log parser
- [ ] Phase 7: Spring AI integration
- [ ] Phase 8: Resilience4j
- [ ] Phase 9: React frontend
- [ ] Phase 10: Dockerization
- [ ] Phase 11: Testing & documentation
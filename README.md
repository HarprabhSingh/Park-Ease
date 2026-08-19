# ParkEasy

ParkEasy is a marketplace for verified, independently bookable parking spaces. The initial product hypothesis targets hourly and daily parking in the Rajouri Garden–Tagore Garden corridor in West Delhi.

The project is being built as a modular Java/Spring backend with a deliberately evidence-driven evolution path. PostgreSQL owns booking truth; infrastructure such as Redis, Kafka, and external payments will be introduced only when a demonstrated problem justifies them.

## Current status

The product, architecture, booking-domain, database, and HTTP API designs are approved. Implementation is beginning with a minimal Java 21 and Spring Boot foundation.

No production or performance claims are made yet.

## Technology direction

- Java 21
- Spring Boot 4.1
- Spring MVC
- Maven
- PostgreSQL
- Flyway
- JUnit 5 and Testcontainers
- Docker Compose for local infrastructure
- React and TypeScript after the backend foundation

## Repository layout

```text
backend/       Spring Boot backend
API.md         HTTP contract
DATABASE.md    Relational model and concurrency design
HLD.md         High-level architecture
LLD.md         Booking-domain low-level design
PRD.md         Product requirements
LEARNING_LOG.md
```

## Design documents

- [Product requirements](./PRD.md)
- [High-level design](./HLD.md)
- [Low-level design](./LLD.md)
- [Database design](./DATABASE.md)
- [API specification](./API.md)
- [Learning log](./LEARNING_LOG.md)

## Prerequisites

- JDK 21
- Maven 3.6.3 or newer
- Docker Desktop with Docker Compose before PostgreSQL/Testcontainers work

## Build

From the repository root:

```shell
mvn -f backend/pom.xml verify
```

`verify` starts its own temporary PostgreSQL through Testcontainers. On this 8 GB development machine, stop the Compose database first to avoid running two PostgreSQL containers during the test:

```shell
docker compose stop postgres
mvn -f backend/pom.xml verify
```

Start local PostgreSQL:

```shell
docker compose up -d --wait postgres
```

ParkEasy maps PostgreSQL to `127.0.0.1:5434` because this workstation already uses host ports `5432` and `5433`. Set `PARKEASY_DB_PORT` and `PARKEASY_DB_URL` together if another port is required.

Run the backend during the foundation phase:

```shell
mvn -f backend/pom.xml spring-boot:run
```

The local health endpoint is expected at `http://localhost:8080/actuator/health` after startup.

Stop local infrastructure without deleting its persistent volume:

```shell
docker compose stop
```

## Engineering principles

- Understanding before speed
- Database-backed correctness
- Explicit module boundaries
- Meaningful tests rather than coverage inflation
- Measurement before optimization
- Real-user evidence before feature expansion
- No invented resume metrics

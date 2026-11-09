# Bank account CQRS / Event Sourcing demo

A Spring Boot 3.4.3 / Java 21 application that follows the practical sequence in the TP: commands are handled by an Axon event-sourced aggregate, events build a JPA read model, queries use Axon's `QueryGateway`, and account operations can be streamed over SSE. The finished configuration uses PostgreSQL for the read model and Axon Server for the event store and message dispatch.

## Run the full setup

Requirements: Java 21, Maven 3.9+, and Docker Compose.

```bash
cp .env.example .env
docker compose up -d
mvn spring-boot:run
```

Run the automated aggregate tests with `mvn test`.

The API listens on port `8066`.

- Swagger UI: <http://localhost:8066/swagger-ui.html>
- Axon Server dashboard: <http://localhost:8024>
- PgAdmin: <http://localhost:8088> (default login `admin@example.com` / `admin`)
- PostgreSQL: `localhost:5432`, database `account_db`, user `admin`, password `1234`
- Axon Server gRPC: `localhost:8124`

The compose file persists PostgreSQL and Axon Server data in named Docker volumes. The `.env.example` values are for local coursework only; replace them for any shared or deployed environment.

## Event store progression from the demonstration

The checked-in POM is the completed configuration: the Axon Server connector is enabled, PostgreSQL stores the query-side projection, and Axon Server stores the event stream. To repeat the demonstration's intermediate PostgreSQL event-store stage, temporarily exclude `axon-server-connector` from `axon-spring-boot-starter`; with JPA on the classpath, Axon uses its embedded event store backed by PostgreSQL. The professor's earlier in-memory stage is before that datasource is added. Remove the temporary exclusion to return to the checked-in Axon Server configuration.

## HTTP API

### Commands

Create an account (the command emits `AccountCreatedEvent` followed by `AccountActivatedEvent`):

```http
POST /commands/account/add
Content-Type: application/json

{"initialBalance":12000,"currency":"MAD"}
```

The response is the generated account ID as a JSON string. Credit or debit an activated account:

```http
POST /commands/account/{accountId}/credit
Content-Type: application/json

{"amount":3000,"currency":"MAD"}
```

```http
POST /commands/account/{accountId}/debit
Content-Type: application/json

{"amount":500,"currency":"MAD"}
```

Change a status (`ACTIVATED`, `SUSPENDED`, `BLOCKED`, or `CREATED`):

```http
PUT /commands/account/{accountId}/status
Content-Type: application/json

{"status":"SUSPENDED"}
```

Initial balances and operation amounts must be positive. Credit and debit require an activated account and the account currency. Debits cannot exceed the current balance. Business-rule and validation errors return HTTP 400.

### Queries

- `GET /query/account/all` — all projected accounts.
- `GET /query/account/{accountId}/statement` — account and its operations in time order.
- `GET /query/account/{accountId}/watch` — Server-Sent Events for the account's operations. Connect with `curl -N http://localhost:8066/query/account/{accountId}/watch`; the existing operations are sent first, followed by each new credit or debit.

Commands are asynchronous and projections are eventually consistent, so a query immediately after a command may briefly return the previous read-model state.

## Structure

```text
src/main/java/ma/tpcqrs/bankaccount/
├── command/
│   ├── aggregates/       # AccountAggregate: command decisions and event-sourced state
│   ├── commands/         # Immutable Axon commands
│   ├── controllers/      # REST command endpoints and CommandGateway
│   ├── dto/              # Validated request records
│   └── events/           # Immutable domain events
├── common/               # Account and operation enums, API error handling
└── query/
    ├── controllers/      # QueryGateway endpoints and SSE subscription
    ├── dto/               # Account statement response
    ├── entities/          # PostgreSQL read model
    ├── handlers/          # Event projection and query handlers
    ├── queries/           # Query messages
    └── repositories/      # Spring Data JPA repositories
```

Event payloads use Axon's Jackson serializer. The event-sourcing aggregate enforces business rules and updates its state only through `@EventSourcingHandler` methods. `AccountEventHandler` projects events into PostgreSQL and uses `QueryUpdateEmitter` to notify active SSE subscriptions.

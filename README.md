# 💳 FinFlow — Distributed Real-Time Payment & Settlement Ledger Engine

[![Java](https://img.shields.io/badge/Java-21-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen.svg)]()
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-KRaft%20Mode-black.svg)]()
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)]()
[![MongoDB](https://img.shields.io/badge/MongoDB-NoSQL%20Audit-green.svg)]()
[![Docker](https://img.shields.io/badge/Docker-Containers-blue.svg)]()

FinFlow is an enterprise-grade distributed payment and settlement engine. It guarantees strict ACID compliance using **Double-Entry Bookkeeping**, eliminates race conditions and deadlocks under concurrent transfers, solves the **Distributed Dual-Write Problem** using the **Transactional Outbox Pattern**, and streams immutable event logs asynchronously to **Apache Kafka** and **MongoDB**.

---

## 🏛️ System Architecture

```text
 [ Client / Frontend / Postman ]
               │
               ▼  POST /api/v1/payments/transfer
 ┌────────────────────────────────────────────────────────────────────────┐
 │ Spring Boot Payment Engine (@Transactional Boundary)                   │
 │                                                                        │
 │  1. Idempotency Check (Prevents duplicate charges)                     │
 │  2. Deterministic Lock Ordering (Eliminates deadlocks: ACC1 vs ACC2)   │
 │  3. Pessimistic Write Locking (SELECT ... FOR UPDATE)                  │
 │  4. Strict Double-Entry Ledger Execution (Sum of Debits == Credits)    │
 │  5. Optimistic Lock Verification (@Version)                            │
 │  6. Transactional Outbox Insert (Status = PENDING)                     │
 └──────────────────────┬─────────────────────────────────────────────────┘
                        │ Atomic ACID Commit
                        ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │ MySQL 8.0 (Transactional Core)                                         │
 │   • accounts (balances & lock versions)                                │
 │   • transactions (transfer intent & idempotency key)                   │
 │   • journal_entries (immutable debit/credit balance snapshots)         │
 │   • outbox_events (reliable domain event buffer)                       │
 └──────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Polled by @Scheduled(fixedRate = 2000)
                        ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │ OutboxPublisher (Guaranteed At-Least-Once Delivery)                   │
 │   • Publishes event to Kafka topic 'payment-events'                    │
 │   • Marks outbox_events status to 'PUBLISHED' in MySQL                 │
 └──────────────────────┬─────────────────────────────────────────────────┘
                        │
                        ▼ TCP / 9092
 ┌────────────────────────────────────────────────────────────────────────┐
 │ Apache Kafka (Docker KRaft Cluster)                                    │
 │   • Topic: "payment-events" (3 Partitions, Key-Based Routing)          │
 │   • Monitored via Kafka UI (http://localhost:8085)                     │
 └──────────────────────┬─────────────────────────────────────────────────┘
                        │
                        │ Consumed by @KafkaListener (Group: finflow-audit-group)
                        ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │ AuditLogConsumer                                                       │
 │   • Deserializes JSON payment events from Kafka                        │
 │   • Writes to NoSQL Document Store asynchronously                      │
 └──────────────────────┬─────────────────────────────────────────────────┘
                        │
                        ▼
 ┌────────────────────────────────────────────────────────────────────────┐
 │ MongoDB (Polyglot Audit Store)                                         │
 │   • Database: finflow_audit                                            │
 │   • Collection: audit_logs (Separates high-volume logs from MySQL)     │
 └────────────────────────────────────────────────────────────────────────┘
```

---

## 💡 Key Engineering Highlights

### 1. Concurrency Control & Deadlock Prevention
* **Pessimistic Locking:** Uses `SELECT ... FOR UPDATE` to lock source and destination account rows during balance updates.
* **Deterministic Lock Ordering:** Avoids classic database deadlocks ($A \to B$ vs $B \to A$) by sorting account numbers lexicographically (`compareTo`) and acquiring locks in a strictly globally consistent order.
* **Optimistic Locking:** Entity `@Version` fields ensure stale in-memory states fail fast with optimistic lock exceptions.

### 2. Transactional Outbox Pattern (Solving Dual-Write)
* Solves the distributed systems problem where updating a database and publishing to a message broker in the same HTTP request causes inconsistent data when either fails.
* The business transaction and an `OutboxEvent` are written to MySQL in the same atomic transaction. A background scheduler polls `PENDING` events, delivers them to Kafka, and marks them `PUBLISHED`.

### 3. Strict Double-Entry Bookkeeping
* Money is never arbitrarily added or removed. Every transfer generates balanced `DEBIT` and `CREDIT` `JournalEntry` rows with a point-in-time `balanceAfter` snapshot.

### 4. Polyglot Persistence
* **MySQL:** Primary ACID store for relational account and journal data.
* **MongoDB:** Asynchronous append-only document store for historical and compliance audit tracking, avoiding database lock contention on transactional tables.

---

## 📡 REST API Reference

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/payments/transfer` | Execute atomic fund transfer (Idempotent) |
| `GET` | `/api/v1/accounts` | Fetch all accounts and live balances |
| `GET` | `/api/v1/accounts/{accountNumber}` | Fetch specific account details |
| `GET` | `/api/v1/payments/transactions` | Fetch all completed transactions |
| `GET` | `/api/v1/payments/journal-entries/{accountNumber}` | Fetch ledger lines for an account |
| `GET` | `/api/v1/audit/logs` | Fetch MongoDB audit events |

---

## 🛠️ Technology Stack

* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot 4.1.2 (Spring MVC, Spring Data JPA, Spring Kafka, Spring Data MongoDB)
* **Primary Relational DB:** MySQL 8.0 (InnoDB)
* **Message Broker:** Apache Kafka (KRaft mode, 3 partitions)
* **Audit Document Store:** MongoDB 8.0
* **Infrastructure:** Docker & Docker Compose
* **Build Tool:** Maven

---

## 🏃 Running Locally

### 1. Start Infrastructure via Docker
```bash
docker compose up -d
```
* Kafka Broker: `localhost:9092`
* Kafka UI: `http://localhost:8085`
* MongoDB: `localhost:27017`

### 2. Run the Application
Open `FinflowBackendApplication.java` in IntelliJ IDEA and run, or via Maven:
```bash
./mvnw spring-boot:run
```

---

## 👨‍💻 Author
**Siva Prasanna**  
* Senior Systems Engineer | Java Full Stack Developer
* GitHub: [@SIVAPRASANNA2812](https://github.com/SIVAPRASANNA2812)
* LinkedIn: [sivaprasanna2812](https://www.linkedin.com/in/sivaprasanna2812)

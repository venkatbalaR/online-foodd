# Online Food Order Processing System

Microservices-based food ordering platform with **Camunda BPMN workflow orchestration** and **ActiveMQ** async messaging.

## Architecture

```
React UI (3000) → Order Service (8081) → ActiveMQ (61616) → Camunda
                                    ↓
                    Payment (8082) / Kitchen (8083) / Delivery (8084)
                                    ↓
                              MySQL (3306)
```

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+
- MySQL 8.x running on `localhost:3306`

## Quick Start

### 1. Start MySQL

Ensure MySQL is running. The app creates `food_order_db` automatically.

Default credentials (in `application.properties`):
- User: `root`
- Password: `123456789`

### 2. Start Backend Services

**Start Order Service first** (it embeds the ActiveMQ broker):

```powershell
cd order-service
mvn spring-boot:run
```

Then start the other three services (separate terminals):

```powershell
cd payment-service
mvn spring-boot:run
```

```powershell
cd kitchen-service
mvn spring-boot:run
```

```powershell
cd delivery-service
mvn spring-boot:run
```

### 3. Start Frontend

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:3000

## URLs

| Component | URL |
|-----------|-----|
| FoodFlow UI | http://localhost:3000 |
| Order Service API | http://localhost:8081/api/orders |
| Camunda Cockpit | http://localhost:8081/camunda (admin / admin) |

## Order Flow

1. Browse menu → Add to cart → Enter name → Place Order
2. Order saved as `PLACED` → Camunda workflow starts
3. Payment → Kitchen → Delivery (via ActiveMQ)
4. Track progress on the **Track** tab (polls every 2s)

**Demo rule:** Orders over **$100** fail payment and are cancelled.

## Project Structure

```
online-food/
├── pom.xml                    # Maven parent (4 microservices)
├── order-service/             # REST API + Camunda + ActiveMQ broker
├── payment-service/           # JMS consumer
├── kitchen-service/           # JMS consumer
├── delivery-service/          # JMS consumer
├── frontend/                  # React FoodFlow UI
├── diagram_1.bpmn             # BPMN workflow diagram
└── docs/
    ├── API-LLD.md             # API & queue contracts
    ├── DATABASE-DESIGN.md     # Schema & ER diagram
    ├── IMPLEMENTATION-REPORT.md
    └── LOG-OUTPUT-EXAMPLE.md  # Sample console logs
```

## Documentation

| Document | Description |
|----------|-------------|
| [API LLD](docs/API-LLD.md) | REST endpoints, JMS message formats, error handling |
| [Database Design](docs/DATABASE-DESIGN.md) | Tables, columns, ER diagram |
| [Implementation Report](docs/IMPLEMENTATION-REPORT.md) | Honest workspace assessment |
| [Log Output Example](docs/LOG-OUTPUT-EXAMPLE.md) | Expected console output per service |

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 19, Vite 8, React Router 7 |
| Backend | Spring Boot 3.2.5, Java 21 |
| Workflow | Camunda 7.21.0 (embedded) |
| Messaging | ActiveMQ 6.1.2 (embedded broker) |
| Database | MySQL 8, Hibernate JPA |

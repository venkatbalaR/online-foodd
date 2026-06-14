# API Low-Level Design (LLD)
## Online Food Order Processing System

**Stack:** React (FoodFlow) + Order Service (REST) + ActiveMQ + Camunda + Payment/Kitchen/Delivery Services  
**Base URL (Order Service):** `http://localhost:8081`

---

## 1. REST API — Order Service (Entry Point)

Only the **Order Service** exposes REST endpoints. Payment, Kitchen, and Delivery are invoked asynchronously via ActiveMQ as part of the Camunda workflow.

### 1.1 Create Order

**`POST /api/orders`**

Creates a new order, persists it with status `PLACED`, and publishes an event to the `order.created` queue to start the Camunda workflow.

**Request headers:**
```
Content-Type: application/json
```

**Request body:**
```json
{
  "customerName": "John Doe",
  "item": "Pepperoni Pizza",
  "amount": 16.99
}
```

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `customerName` | string | Yes | Max 100 chars |
| `item` | string | Yes | Max 100 chars (comma-separated for multiple items) |
| `amount` | number | Yes | Decimal, precision 10 scale 2 |

**Success response — `201 Created`:**
```json
{
  "id": 5,
  "customerName": "John Doe",
  "item": "Pepperoni Pizza",
  "amount": 16.99,
  "status": "PLACED",
  "createdAt": "2026-06-14T10:30:00"
}
```

**Error response — `500 Internal Server Error`:**
```json
null
```
*(No error body is returned; exception is swallowed in controller.)*

**Side effects:**
1. Row inserted into `orders` table with `status = PLACED`
2. Message published to ActiveMQ queue `order.created`:
   ```json
   {"orderId": 5}
   ```
3. Console log: `[OrderService] Order #5 - PLACED`

---

### 1.2 List All Orders

**`GET /api/orders`**

Returns all orders with their current status. Used by the Track and Kitchen UI pages (polled every 2 seconds).

**Success response — `200 OK`:**
```json
[
  {
    "id": 5,
    "customerName": "John Doe",
    "item": "Pepperoni Pizza",
    "amount": 16.99,
    "status": "DELIVERED",
    "createdAt": "2026-06-14T10:30:00"
  },
  {
    "id": 4,
    "customerName": "Alice",
    "item": "Smoky BBQ Burger x2",
    "amount": 29.98,
    "status": "KITCHEN_PREPARING",
    "createdAt": "2026-06-14T10:25:00"
  }
]
```

**Error response — `500 Internal Server Error`:**
```json
null
```

**Possible `status` values:**
| Status | Meaning |
|--------|---------|
| `PLACED` | Order created, workflow starting |
| `PAID` | Payment succeeded |
| `KITCHEN_PREPARING` | Kitchen is preparing food |
| `OUT_FOR_DELIVERY` | Driver assigned, en route |
| `DELIVERED` | Order completed |
| `CANCELLED` | Payment failed or workflow cancelled |

---

### 1.3 Get Order by ID

**`GET /api/orders/{id}`**

Returns a single order by primary key.

**Path parameter:**
| Name | Type | Example |
|------|------|---------|
| `id` | long | `5` |

**Success response — `200 OK`:**
```json
{
  "id": 5,
  "customerName": "John Doe",
  "item": "Pepperoni Pizza",
  "amount": 16.99,
  "status": "DELIVERED",
  "createdAt": "2026-06-14T10:30:00"
}
```

**Not found — `404 Not Found`:** Empty body

**Error response — `500 Internal Server Error`:** Empty body

---

### 1.4 CORS

All endpoints allow cross-origin requests:
```java
@CrossOrigin(origins = "*")
```

Frontend calls `http://localhost:8081` directly from `http://localhost:3000`.

---

## 2. ActiveMQ Queue Message Formats

**Broker:** Embedded in Order Service — `tcp://localhost:61616`  
**Message format:** JSON string (plain text JMS body)

### 2.1 `order.created`

Triggers Camunda workflow start.

| Role | Service |
|------|---------|
| **Publisher** | Order Service (`OrderController`) |
| **Consumer** | Order Service (`ActiveMQResponseListener.onOrderCreated`) |

**Message:**
```json
{"orderId": 5}
```

**Consumer action:** Starts Camunda process `food-order-process` with variable `orderId`.

---

### 2.2 `payment.request`

Camunda Step 1 — payment processing.

| Role | Service |
|------|---------|
| **Publisher** | Order Service (`PaymentProcessingDelegate`) |
| **Consumer** | Payment Service (`PaymentListener`) |

**Message:**
```json
{
  "orderId": 5,
  "amount": 16.99,
  "correlationId": "payment-5-1718356200123"
}
```

**Consumer action:**
1. Mock payment: `SUCCESS` if `amount <= 100`, else `FAILED`
2. Save row to `payments` table
3. Reply on `payment.response`

---

### 2.3 `payment.response`

| Role | Service |
|------|---------|
| **Publisher** | Payment Service |
| **Consumer** | Order Service (`ActiveMQResponseListener.onPaymentResponse`) |

**Message:**
```json
{
  "orderId": 5,
  "correlationId": "payment-5-1718356200123",
  "status": "SUCCESS"
}
```

**`status` values:** `SUCCESS` | `FAILED`

**Consumer action:** Unblocks `PaymentProcessingDelegate` via `ResponseCoordinator`. Sets Camunda variable `paymentSuccess`.

---

### 2.4 `kitchen.request`

Camunda Step 2 — food preparation (only if payment succeeded).

| Role | Service |
|------|---------|
| **Publisher** | Order Service (`KitchenPrepDelegate`) |
| **Consumer** | Kitchen Service (`KitchenListener`) |

**Message:**
```json
{
  "orderId": 5,
  "item": "Pepperoni Pizza",
  "correlationId": "kitchen-5-1718356201456"
}
```

**Consumer action:**
1. Save ticket with status `PREPARING`
2. Simulate 2-second prep delay
3. Update ticket to `READY`
4. Reply on `kitchen.response`

---

### 2.5 `kitchen.response`

| Role | Service |
|------|---------|
| **Publisher** | Kitchen Service |
| **Consumer** | Order Service (`ActiveMQResponseListener.onKitchenResponse`) |

**Message:**
```json
{
  "orderId": 5,
  "correlationId": "kitchen-5-1718356201456",
  "status": "READY"
}
```

**Consumer action:** Unblocks `KitchenPrepDelegate`.

---

### 2.6 `delivery.request`

Camunda Step 3 — delivery dispatch.

| Role | Service |
|------|---------|
| **Publisher** | Order Service (`DeliveryDispatchDelegate`) |
| **Consumer** | Delivery Service (`DeliveryListener`) |

**Message:**
```json
{
  "orderId": 5,
  "correlationId": "delivery-5-1718356203789"
}
```

**Consumer action:**
1. Assign random driver from fixed list
2. Save delivery with status `ASSIGNED`
3. Simulate 3-second delivery delay
4. Update to `DELIVERED`
5. Reply on `delivery.response`

---

### 2.7 `delivery.response`

| Role | Service |
|------|---------|
| **Publisher** | Delivery Service |
| **Consumer** | Order Service (`ActiveMQResponseListener.onDeliveryResponse`) |

**Message:**
```json
{
  "orderId": 5,
  "correlationId": "delivery-5-1718356203789",
  "status": "DELIVERED",
  "driverName": "Ravi Kumar"
}
```

**Consumer action:** Unblocks `DeliveryDispatchDelegate`. Sets Camunda variable `driverName`.

---

## 3. Correlation ID Pattern

Each async request/response pair uses a unique `correlationId`:

| Step | Pattern | Example |
|------|---------|---------|
| Payment | `payment-{orderId}-{timestamp}` | `payment-5-1718356200123` |
| Kitchen | `kitchen-{orderId}-{timestamp}` | `kitchen-5-1718356201456` |
| Delivery | `delivery-{orderId}-{timestamp}` | `delivery-5-1718356203789` |

`ResponseCoordinator` registers a `CountDownLatch` per correlation ID. Response listeners call `resolve()` to unblock the waiting Camunda delegate.

**Timeouts:**
| Delegate | Timeout |
|----------|---------|
| Payment | 60 seconds |
| Kitchen | 120 seconds |
| Delivery | 120 seconds |

On timeout, `await()` returns `null` → payment treated as failed.

---

## 4. Error Handling & Edge Cases

### 4.1 Payment failure (amount > $100)

| Step | Behavior |
|------|----------|
| Payment Service | Returns `status: "FAILED"` |
| Camunda gateway | Routes to `CANCEL ORDER` task |
| Order status | Set to `CANCELLED` |
| UI | Track tab shows cancelled state |

Frontend shows warning in cart: *"Orders over $100 will fail payment (demo rule)"*

### 4.2 Order Service unavailable

| Symptom | UI message |
|---------|------------|
| POST fails | `"Failed to place order. Is order-service running?"` |
| GET fails | `"Cannot reach order-service. Is it running on port 8081?"` |

### 4.3 Downstream service unavailable

If Payment/Kitchen/Delivery service is down:
- Camunda delegate blocks until timeout
- Payment timeout → treated as failure → order `CANCELLED`
- Kitchen/Delivery timeout → workflow may stall or fail

### 4.4 Invalid order ID

`GET /api/orders/{id}` returns `404` if not found.

### 4.5 JMS parse errors

Listeners catch exceptions, log to `System.err`, and do not rethrow. No dead-letter queue configured.

### 4.6 Missing correlation latch

If response arrives with unknown `correlationId`:
```
Warning: No latch found for correlationId: <id>
```

### 4.7 Duplicate orders

No idempotency key. Each POST creates a new order and starts a new workflow instance.

---

## 5. Camunda Workflow API (Internal)

Not called by the React UI. Started automatically via `order.created` queue.

| Property | Value |
|----------|-------|
| Process key | `food-order-process` |
| Process name | `DELIVERY` |
| Start variable | `orderId` (Long) |
| Gateway variable | `paymentSuccess` (Boolean) |
| Cockpit URL | `http://localhost:8081/camunda` |
| Credentials | `admin` / `admin` |

**BPMN flow:**
```
START → PAYMENT → [paymentSuccess?]
                    ├─ true  → KITCHEN PREPARATION → DELIVERY → UPDATE ORDER STATUS → DELIVERED
                    └─ false → CANCEL ORDER → CANCELLED
```

---

## 6. Service Port Summary

| Service | Port | Protocol |
|---------|------|----------|
| React Frontend (FoodFlow) | 3000 | HTTP |
| Order Service | 8081 | HTTP + JMS |
| Payment Service | 8082 | JMS only |
| Kitchen Service | 8083 | JMS only |
| Delivery Service | 8084 | JMS only |
| ActiveMQ Broker | 61616 | TCP |
| MySQL | 3306 | TCP |
| Camunda Cockpit | 8081/camunda | HTTP |

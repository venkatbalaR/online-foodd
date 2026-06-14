# Log Output — Order Processing Flow (Example)

Capture this output from your terminal when demonstrating a successful order (amount ≤ $100).

**Test order:** Pepperoni Pizza, $16.99, customer "deepu"

---

## Expected Console Output

### Order Service (port 8081)
```
ActiveMQ Embedded Broker started on tcp://localhost:61616
[OrderService] Order #5 - PLACED
[OrderService] Order #5 - Workflow started
[OrderService] Order #5 - Payment result: SUCCESS
[OrderService] Order #5 - Order DELIVERED
```

### Payment Service (port 8082)
```
[PaymentService] Order #5 - Payment SUCCESS
```

### Kitchen Service (port 8083)
```
[KitchenService] Order #5 - Food READY
```

### Delivery Service (port 8084)
```
[DeliveryService] Order #5 - Out for Delivery (Driver: Ravi Kumar)
[DeliveryService] Order #5 - Delivered
```

---

## Cancelled Order Example (amount > $100)

### Order Service
```
[OrderService] Order #6 - PLACED
[OrderService] Order #6 - Workflow started
[OrderService] Order #6 - Payment result: FAILED
[OrderService] Order #6 - Order CANCELLED
```

### Payment Service
```
[PaymentService] Order #6 - Payment FAILED
```

*(Kitchen and Delivery services produce no logs — workflow stops at payment gateway.)*

---

## How to Capture

1. Start all services (see `README.md`).
2. Open four terminal windows — one per microservice.
3. Place an order from http://localhost:3000 (cart → Place Order).
4. Copy console output from each terminal.
5. Screenshot or save as text for interview submission.

---

## Log Reference

| Service | Log prefix | Key messages |
|---------|------------|--------------|
| Order Service | `[OrderService]` | PLACED, Workflow started, Payment result, DELIVERED, CANCELLED |
| Payment Service | `[PaymentService]` | Payment SUCCESS / FAILED |
| Kitchen Service | `[KitchenService]` | Food READY |
| Delivery Service | `[DeliveryService]` | Out for Delivery, Delivered |

package com.example.foodorder.delivery.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "driver_name", length = 100)
    private String driverName;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Convenience constructor used by the service
    public Delivery(Long orderId, String driverName, String deliveryStatus) {
        this.orderId = orderId;
        this.driverName = driverName;
        this.deliveryStatus = deliveryStatus;
    }
}

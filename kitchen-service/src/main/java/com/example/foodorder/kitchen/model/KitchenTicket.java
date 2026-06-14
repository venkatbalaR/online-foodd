package com.example.foodorder.kitchen.model;

import jakarta.persistence.*;

@Entity
@Table(name = "kitchen_tickets")
public class KitchenTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "kitchen_status", length = 50)
    private String kitchenStatus;

    // Constructors
    public KitchenTicket() {}

    public KitchenTicket(Long orderId, String kitchenStatus) {
        this.orderId = orderId;
        this.kitchenStatus = kitchenStatus;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getKitchenStatus() {
        return kitchenStatus;
    }

    public void setKitchenStatus(String kitchenStatus) {
        this.kitchenStatus = kitchenStatus;
    }
}

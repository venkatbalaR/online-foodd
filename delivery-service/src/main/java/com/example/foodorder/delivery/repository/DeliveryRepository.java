package com.example.foodorder.delivery.repository;

import com.example.foodorder.delivery.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByOrderId(Long orderId);

    Optional<Delivery> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
}

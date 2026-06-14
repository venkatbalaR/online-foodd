package com.example.foodorder.kitchen.repository;

import com.example.foodorder.kitchen.model.KitchenTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, Long> {
}

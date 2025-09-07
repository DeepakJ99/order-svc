package com.example.order_svc.model.DAO;


import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    String menuItemId;

    @Column(nullable = false)
    Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;
}

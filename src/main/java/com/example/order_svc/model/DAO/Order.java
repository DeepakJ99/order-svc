package com.example.order_svc.model.DAO;

import com.squiggy.api_spec.DTO.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;
import java.util.List;
@Entity
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;
    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String restaurantId;

    @Column
    private String deliveryId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @Column(nullable = false)
    private Double total;

    private Date createdAt;
    private Date pickedUpAt;
    private Date completedAt;
    private Date cancelledAt;

    @Column(nullable = false)
    private OrderStatus finalStatus;
}

package com.example.order_svc.model.DTO;

import com.squiggy.api_spec.DTO.OrderStatus;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@Data
public class OrderCached implements Serializable {
     UUID orderId;
     String userId;
     String restaurantId;
     String deliveryId;
     List<OrderItemCached> orderItems;
     Double total;
     Date createdAt;
     Date pickedUpAt;
     Date deliveredAt;
     Date cancelledAt;
     OrderStatus currentStatus;
}

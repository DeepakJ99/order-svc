package com.example.order_svc.model.DTO;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Value
public class OrderItemCached implements Serializable {
    UUID id;
    String menuItemId;
    Integer quantity;
}

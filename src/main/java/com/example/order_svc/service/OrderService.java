package com.example.order_svc.service;

import com.example.order_svc.model.DAO.Order;
import com.example.order_svc.model.DTO.OrderCached;
import com.example.order_svc.model.DTO.OrderItemCached;
import com.example.order_svc.repository.OrderRepository;
import com.squiggy.api_spec.DTO.OrderStatus;
import com.squiggy.api_spec.DTO.event.OrderInitiationEvent;
import com.squiggy.api_spec.DTO.event.OrderStatusUpdateEvent;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class OrderService {

    @Getter
    @Setter
    Boolean deliveryPartnerAvailable = true;
    @Autowired
    private OrderRepository orderRepository;

    public void saveArchive(Order order){
        try {
            orderRepository.save(order);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}

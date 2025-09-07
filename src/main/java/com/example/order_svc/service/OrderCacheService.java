package com.example.order_svc.service;

import com.example.order_svc.model.DAO.Order;
import com.example.order_svc.model.DAO.OrderItem;
import com.example.order_svc.model.DTO.OrderCached;
import com.example.order_svc.model.DTO.OrderItemCached;
import com.squiggy.api_spec.DTO.OrderStatus;
import com.squiggy.api_spec.DTO.event.OrderInitiationEvent;
import com.squiggy.api_spec.DTO.event.OrderStatusUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.squiggy.api_spec.DTO.OrderStatus.*;

@Service
public class OrderCacheService {

    private static final Integer TTL = 2;
    @Autowired
    private RedisTemplate<String, OrderCached> redisTemplate;

    @Autowired
    private OrderService orderService;
    public OrderCached getOrder(UUID orderId) {
        return redisTemplate.opsForValue().get(orderId.toString());
    }

    public void createCachedOrder(OrderInitiationEvent orderInitiationEvent) {
        OrderCached orderCached = new OrderCached();
        orderCached.setCreatedAt(new Date());
        orderCached.setOrderId(UUID.fromString(orderInitiationEvent.getOrderId()));
        orderCached.setUserId(orderInitiationEvent.getUserId());
        orderCached.setRestaurantId(orderInitiationEvent.getRestaurantId());
        List<OrderItemCached> itemsCached = new ArrayList<>();
        orderInitiationEvent.getMenuItems().forEach((id, qty)->{
            itemsCached.add(new OrderItemCached(UUID.randomUUID(), id, qty));
        });
        orderCached.setOrderItems(itemsCached);
        orderCached.setCurrentStatus(OrderStatus.INITIATED);
        cacheOrder(orderCached);
    }

    public void updateCachedOrder(OrderStatusUpdateEvent orderStatusUpdateEvent) {
        updateOrderCachedStatus(orderStatusUpdateEvent.getOrderId(), orderStatusUpdateEvent.getOrderStatus());
    }
    public void cacheOrder(OrderCached orderCached) {
        redisTemplate.opsForValue().set(orderCached.getOrderId().toString(), orderCached, Duration.ofHours(TTL));
    }

    public void updateOrderCachedStatus(String orderId, OrderStatus orderStatus) {
        OrderCached orderCached = redisTemplate.opsForValue().get(orderId);
        if(orderCached != null){
            orderCached.setCurrentStatus(orderStatus);
            if(orderStatus == DELIVERED || orderStatus == OrderStatus.CANCELLED){
                Order order = archiveOrder(orderCached);
                orderService.saveArchive(order);
                redisTemplate.delete(orderId);

            }else{
                redisTemplate.opsForValue().set(orderId, orderCached);
            }
        }
    }

    Order archiveOrder(OrderCached orderCached) {
        Order order = new Order();
        order.setOrderId(orderCached.getOrderId());
        order.setUserId(orderCached.getUserId());
        order.setRestaurantId(orderCached.getRestaurantId());
        order.setTotal(orderCached.getTotal());
        order.setDeliveryId(orderCached.getDeliveryId());
        List<OrderItem> orderItems = orderCached.getOrderItems().stream()
                .map(orderItemCached -> {
                    OrderItem item = new OrderItem();
                    item.setId(orderItemCached.getId());
                    item.setQuantity(orderItemCached.getQuantity());
                    item.setOrder(order);
                    item.setMenuItemId(orderItemCached.getMenuItemId());
                    return item;
                }).toList();
        order.setOrderItems(orderItems);
        if(orderCached.getCurrentStatus() == COMPLETED){
            order.setCompletedAt(orderCached.getDeliveredAt());
            order.setFinalStatus(COMPLETED);
        }else{
            order.setCompletedAt(orderCached.getCancelledAt());
            order.setFinalStatus(CANCELLED);
        }
        return order;
    }
}

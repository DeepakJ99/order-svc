package com.example.order_svc.service.event;

import com.example.order_svc.service.OrderCacheService;
import com.example.order_svc.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squiggy.api_spec.DTO.DeliveryPartnerStatus;
import com.squiggy.api_spec.DTO.OrderStatus;
import com.squiggy.api_spec.DTO.event.DeliveryPartnerEvent;
import com.squiggy.api_spec.DTO.event.OrderInitiationEvent;
import com.squiggy.api_spec.DTO.event.OrderStatusUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EventPubSub {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    OrderCacheService orderCacheService;

    @Autowired
    OrderService orderService;
    public EventPubSub(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = {"users-channel", "orders-channel"}, groupId = "order-svc")
    public void consumeOrder(String jsonString) throws JsonProcessingException {
        System.out.println("Consumed JSON: " + jsonString);
        try{
            OrderStatusUpdateEvent orderRelatedEvent = objectMapper.readValue(jsonString, OrderStatusUpdateEvent.class);
            if(orderService.getDeliveryPartnerAvailable()){
                orderCacheService.updateCachedOrder(orderRelatedEvent);
                if(orderRelatedEvent.getOrderStatus() == OrderStatus.DELIVERED || orderRelatedEvent.getOrderStatus() == OrderStatus.CANCELLED){
                    produce("orders-channel", OrderStatus.COMPLETED);
                }
            }
        } catch (JsonProcessingException e) {
            OrderInitiationEvent orderInitiationEvent = objectMapper.readValue(jsonString, OrderInitiationEvent.class);
            orderCacheService.createCachedOrder(orderInitiationEvent);
            produce(orderInitiationEvent.getOrderId(), OrderStatus.CONFIRMED);
        }
    }

    @KafkaListener(topics = "riders-channel", groupId = "order-svc")
    public void consumeDelivery(String jsonString) throws JsonProcessingException {
        System.out.println("jsonString: " + jsonString);
        DeliveryPartnerEvent deliveryRelatedEvent = objectMapper.readValue(jsonString, DeliveryPartnerEvent.class);
        if(deliveryRelatedEvent.getStatus() == DeliveryPartnerStatus.READY){
            // delivery partners ready, go for order confirmation
            orderService.setDeliveryPartnerAvailable(true);
        }
    }

    public void produce(String orderId, OrderStatus status) throws JsonProcessingException {
        OrderStatusUpdateEvent event = new OrderStatusUpdateEvent(orderId, status);
        kafkaTemplate.send("orders-channel", objectMapper.writeValueAsString(event));
    }

}

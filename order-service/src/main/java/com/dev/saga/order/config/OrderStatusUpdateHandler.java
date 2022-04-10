package com.dev.saga.order.config;

import com.dev.saga.common.dto.OrderRequestDto;
import com.dev.saga.common.event.OrderStatus;
import com.dev.saga.common.event.PaymentStatus;
import com.dev.saga.order.entity.PurchaseOrder;
import com.dev.saga.order.repository.OrderRepository;
import com.dev.saga.order.service.OrderStatusPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Configuration
public class OrderStatusUpdateHandler {
    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderStatusPublisher publisher;

    @Transactional
    public void updateOrder(int id, Consumer<PurchaseOrder> consumer) {
        repository.findById(id).ifPresent(consumer.andThen(this::updateOrder));
    }

    private void updateOrder(PurchaseOrder purchaseOrder) {
        boolean isPaymentCompleted = PaymentStatus.PAYMENT_COMPLETED.equals(purchaseOrder.getPaymentStatus());
        OrderStatus orderStatus = isPaymentCompleted ? OrderStatus.ORDER_COMPLETED : OrderStatus.ORDER_CANCELLED;
        purchaseOrder.setOrderStatus(orderStatus);
        if (!isPaymentCompleted) {
            publisher.publishOrderEvent(convertEntityToDto(purchaseOrder), orderStatus);
        }
    }

    public OrderRequestDto convertEntityToDto(PurchaseOrder purchaseOrder) {
        return new OrderRequestDto(
                purchaseOrder.getUserId(),
                purchaseOrder.getProductId(),
                purchaseOrder.getPrice(),
                purchaseOrder.getId()
        );
    }
}

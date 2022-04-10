package com.dev.saga.order.service;

import com.dev.saga.common.dto.OrderRequestDto;
import com.dev.saga.common.event.OrderStatus;
import com.dev.saga.order.entity.PurchaseOrder;
import com.dev.saga.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository repository;

    @Autowired
    private OrderStatusPublisher publisher;

    @Transactional
    public PurchaseOrder createOrder(OrderRequestDto orderRequestDto) {
        PurchaseOrder purchaseOrder = repository.save(convertDtoToEntity(orderRequestDto));
        orderRequestDto.setOrderId(purchaseOrder.getId());

        // produce kafka event with status ORDER_CREATED
        publisher.publishOrderEvent(orderRequestDto, OrderStatus.ORDER_CREATED);

        return purchaseOrder;
    }

    public List<PurchaseOrder> getAllOrders() {
        return repository.findAll();
    }

    private PurchaseOrder convertDtoToEntity(OrderRequestDto orderRequestDto) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setProductId(orderRequestDto.getProductId());
        purchaseOrder.setUserId(orderRequestDto.getUserId());
        purchaseOrder.setOrderStatus(OrderStatus.ORDER_CREATED);
        purchaseOrder.setPrice(orderRequestDto.getAmount());
        return purchaseOrder;
    }
}

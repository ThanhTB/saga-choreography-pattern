package com.dev.saga.order.controller;

import com.dev.saga.common.dto.OrderRequestDto;
import com.dev.saga.order.entity.PurchaseOrder;
import com.dev.saga.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService service;

    @GetMapping
    public List<PurchaseOrder> getAllOrders() {
        return service.getAllOrders();
    }

    @PostMapping
    public PurchaseOrder createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        return service.createOrder(orderRequestDto);
    }
}

package com.dev.saga.payment.config;

import com.dev.saga.common.event.OrderEvent;
import com.dev.saga.common.event.OrderStatus;
import com.dev.saga.common.event.PaymentEvent;
import com.dev.saga.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class PaymentConsumerConfig {
    @Autowired
    private PaymentService service;

    @Bean
    public Function<Flux<OrderEvent>, Flux<PaymentEvent>> paymentProcessor() {
        return orderEventFlux -> orderEventFlux.flatMap(this::processPayment);
    }

    private Mono<PaymentEvent> processPayment(OrderEvent orderEvent) {
        if (OrderStatus.ORDER_CREATED.equals(orderEvent.getOrderStatus())) {
            return Mono.fromSupplier(() -> this.service.newOrderEvent(orderEvent));
        }

        return Mono.fromRunnable(() -> this.service.cancelOrderEvent(orderEvent));
    }
}

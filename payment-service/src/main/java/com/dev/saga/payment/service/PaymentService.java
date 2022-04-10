package com.dev.saga.payment.service;

import com.dev.saga.common.dto.OrderRequestDto;
import com.dev.saga.common.dto.PaymentRequestDto;
import com.dev.saga.common.event.OrderEvent;
import com.dev.saga.common.event.PaymentEvent;
import com.dev.saga.common.event.PaymentStatus;
import com.dev.saga.payment.entity.UserBalance;
import com.dev.saga.payment.entity.UserTransaction;
import com.dev.saga.payment.repository.UserBalanceRepository;
import com.dev.saga.payment.repository.UserTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PaymentService {
    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    @PostConstruct
    public void initUserBalanceInDB() {
        userBalanceRepository.saveAll(Stream.of(
                new UserBalance(101, 5000),
                new UserBalance(102, 3000),
                new UserBalance(103, 4200),
                new UserBalance(104, 20000),
                new UserBalance(105, 999)
        ).collect(Collectors.toList()));
    }

    /***
     * 1. Get the UserId
     * 2. check the balance availability
     * 3. if balance sufficient -> Payment completed and deduct amount from DB
     * 4. if payment not sufficient -> cancel order event and update the amount in DB
     */
    @Transactional
    public PaymentEvent newOrderEvent(OrderEvent orderEvent) {
        OrderRequestDto orderRequestDto = orderEvent.getOrderRequestDto();
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(orderRequestDto.getOrderId(), orderRequestDto.getUserId(), orderRequestDto.getAmount());

        return userBalanceRepository.findById(paymentRequestDto.getUserId())
                .filter(ub -> ub.getPrice() > orderRequestDto.getAmount())
                .map(ub -> {
                    ub.setPrice(ub.getPrice() - orderRequestDto.getAmount());
                    userTransactionRepository.save(new UserTransaction(orderRequestDto.getOrderId(), orderRequestDto.getUserId(), orderRequestDto.getAmount()));
                    return new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_COMPLETED);
                }).orElse(new PaymentEvent(paymentRequestDto, PaymentStatus.PAYMENT_FAILED));
    }

    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {
        userTransactionRepository.findById(orderEvent
                        .getOrderRequestDto()
                        .getOrderId())
                .ifPresent(ut -> {
                    userTransactionRepository.delete(ut);
                    userTransactionRepository.findById(ut.getUserId()).ifPresent(ub -> ub.setAmount(ub.getAmount() + ut.getAmount()));
                });
    }
}

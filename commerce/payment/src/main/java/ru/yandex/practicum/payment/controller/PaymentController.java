package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.PaymentApi;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.payment.service.PaymentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    @PostMapping("/api/v1/payment")
    public PaymentDto payment(@RequestBody OrderDto order) {
        return paymentService.payment(order);
    }

    @Override
    @PostMapping("/api/v1/payment/totalCost")
    public double getTotalCost(@RequestBody OrderDto order) {
        return paymentService.getTotalCost(order);
    }

    @Override
    @PostMapping("/api/v1/payment/productCost")
    public double productCost(@RequestBody OrderDto order) {
        return paymentService.productCost(order);
    }

    @Override
    @PostMapping("/api/v1/payment/refund")
    public void paymentSuccess(@RequestBody UUID paymentId) {
        paymentService.paymentSuccess(paymentId);
    }

    @Override
    @PostMapping("/api/v1/payment/failed")
    public void paymentFailed(@RequestBody UUID paymentId) {
        paymentService.paymentFailed(paymentId);
    }
}
package ru.yandex.practicum.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.feign.OrderFeignClient;
import ru.yandex.practicum.feign.ShoppingStoreFeignClient;
import ru.yandex.practicum.model.PaymentStatus;
import ru.yandex.practicum.payment.entity.Payment;
import ru.yandex.practicum.payment.mapper.PaymentMapper;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ShoppingStoreFeignClient storeClient;
    private final OrderFeignClient orderClient;
    private final PaymentMapper paymentMapper;
    private final double taxRate;

    public PaymentService(PaymentRepository paymentRepository,
                          ShoppingStoreFeignClient storeClient,
                          OrderFeignClient orderClient,
                          PaymentMapper paymentMapper,
                          @Value("${payment.tax-rate:0.1}") double taxRate) {
        this.paymentRepository = paymentRepository;
        this.storeClient = storeClient;
        this.orderClient = orderClient;
        this.paymentMapper = paymentMapper;
        this.taxRate = taxRate;
    }

    public double productCost(OrderDto order) {
        double sum = 0;
        for (Map.Entry<UUID, Long> entry : order.getProducts().entrySet()) {
            ProductDto product = storeClient.getProduct(entry.getKey());
            sum += product.getPrice().doubleValue() * entry.getValue();
        }
        return sum;
    }

    public double getTotalCost(OrderDto order) {
        double productCost = productCost(order);
        double tax = productCost * taxRate;
        return productCost + tax + order.getDeliveryPrice();
    }

    @Transactional
    public PaymentDto payment(OrderDto order) {
        double productCost = productCost(order);
        double deliveryCost = order.getDeliveryPrice();
        double tax = productCost * taxRate;
        double total = productCost + tax + deliveryCost;

        Payment payment = paymentMapper.toEntity(order, productCost, deliveryCost, tax, total);
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(PaymentStatus.SUCCESS);
        orderClient.paymentSuccess(payment.getOrderId());
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus(PaymentStatus.FAILED);
        orderClient.paymentFailed(payment.getOrderId());
    }
}
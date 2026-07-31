package ru.yandex.practicum.payment.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.PaymentDto;
import ru.yandex.practicum.model.PaymentStatus;
import ru.yandex.practicum.payment.entity.Payment;

@Component
public class PaymentMapper {

    public Payment toEntity(OrderDto order, double productCost, double deliveryCost, double tax, double total) {
        Payment payment = new Payment();
        payment.setOrderId(order.getOrderId());
        payment.setProductCost(productCost);
        payment.setDeliveryCost(deliveryCost);
        payment.setTax(tax);
        payment.setTotalCost(total);
        payment.setStatus(PaymentStatus.PENDING);
        return payment;
    }

    public PaymentDto toDto(Payment payment) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(payment.getId());
        dto.setTotalPayment(payment.getTotalCost());
        dto.setDeliveryTotal(payment.getDeliveryCost());
        dto.setFeeTotal(payment.getTax());
        return dto;
    }
}

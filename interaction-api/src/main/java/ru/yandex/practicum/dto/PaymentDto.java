package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PaymentDto {
    private UUID paymentId;
    private double totalPayment;
    private double deliveryTotal;
    private double feeTotal;
}

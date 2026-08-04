package ru.yandex.practicum.payment.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.model.PaymentStatus;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID orderId;
    private double productCost;
    private double deliveryCost;
    private double tax;
    private double totalCost;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
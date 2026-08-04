package ru.yandex.practicum.delivery.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.model.DeliveryState;
import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Data
public class Delivery {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID orderId;
    @Embedded
    private AddressEmbeddable fromAddress;
    @Embedded
    private AddressEmbeddable toAddress;
    private double weight;
    private double volume;
    private boolean fragile;
    @Enumerated(EnumType.STRING)
    private DeliveryState status;
}
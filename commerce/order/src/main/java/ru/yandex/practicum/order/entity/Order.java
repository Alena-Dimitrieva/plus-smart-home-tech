package ru.yandex.practicum.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.model.OrderState;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID shoppingCartId;

    @Convert(converter = ProductMapConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<UUID, Long> products;

    private UUID paymentId;
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    private OrderState state;

    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
    private double totalPrice;
    private double deliveryPrice;
    private double productPrice;

    @Embedded
    private AddressEmbeddable deliveryAddress;

    private String username;
}

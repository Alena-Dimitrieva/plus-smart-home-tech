package ru.yandex.practicum.warehouse.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
@Data
public class OrderBooking {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID orderId;
    private UUID productId;
    private long quantity;
    private UUID deliveryId;
}

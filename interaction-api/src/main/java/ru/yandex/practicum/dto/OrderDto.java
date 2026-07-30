package ru.yandex.practicum.dto;

import lombok.Data;
import ru.yandex.practicum.model.OrderState;
import java.util.Map;
import java.util.UUID;

@Data
public class OrderDto {
    private UUID orderId;
    private UUID shoppingCartId;
    private Map<UUID, Long> products;
    private UUID paymentId;
    private UUID deliveryId;
    private OrderState state;
    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
    private double totalPrice;
    private double deliveryPrice;
    private double productPrice;
    private AddressDto deliveryAddress;
}

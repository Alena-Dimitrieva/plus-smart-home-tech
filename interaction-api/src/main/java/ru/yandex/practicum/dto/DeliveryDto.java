package ru.yandex.practicum.dto;

import lombok.Data;
import ru.yandex.practicum.model.DeliveryState;
import java.util.UUID;

@Data
public class DeliveryDto {
    private UUID deliveryId;
    private UUID orderId;
    private AddressDto fromAddress;
    private AddressDto toAddress;
    private DeliveryState deliveryState;
}

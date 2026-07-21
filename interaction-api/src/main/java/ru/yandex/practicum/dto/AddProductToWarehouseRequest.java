package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AddProductToWarehouseRequest {
    @NotNull(message = "productId не должен быть null")
    private UUID productId;

    @Min(value = 1, message = "quantity должен быть > 0")
    private long quantity;
}

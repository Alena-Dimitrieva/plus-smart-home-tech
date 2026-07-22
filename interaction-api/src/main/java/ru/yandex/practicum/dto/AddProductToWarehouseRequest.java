package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddProductToWarehouseRequest {
    private java.util.UUID productId;

    @NotNull
    @Min(value = 1, message = "quantity должен быть > 0")
    private Long quantity;
}
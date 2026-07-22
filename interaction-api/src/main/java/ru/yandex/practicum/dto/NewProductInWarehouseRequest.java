package ru.yandex.practicum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class NewProductInWarehouseRequest {
    @NotNull(message = "productId не должен быть null")
    private UUID productId;

    private Boolean fragile;

    @Valid
    @NotNull(message = "dimension не должен быть null")
    private DimensionDto dimension;

    @NotNull
    @Min(value = 1, message = "weight должен быть > 0")
    private double weight;
}
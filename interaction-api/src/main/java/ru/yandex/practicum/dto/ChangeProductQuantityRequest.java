package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ChangeProductQuantityRequest {
    @NotNull(message = "productId не должен быть null")
    private UUID productId;

    @Min(value = 0, message = "newQuantity не может быть отрицательным")
    private long newQuantity;
}
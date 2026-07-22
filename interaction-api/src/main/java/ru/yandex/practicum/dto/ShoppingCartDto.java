package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;
import java.util.UUID;

@Data
public class ShoppingCartDto {
    @NotNull
    private UUID shoppingCartId;

    @NotNull
    private Map<UUID, Long> products;
}
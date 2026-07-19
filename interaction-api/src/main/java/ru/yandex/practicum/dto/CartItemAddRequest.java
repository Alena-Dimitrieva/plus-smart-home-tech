package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CartItemAddRequest {
    private UUID productId;
    private int quantity;
}

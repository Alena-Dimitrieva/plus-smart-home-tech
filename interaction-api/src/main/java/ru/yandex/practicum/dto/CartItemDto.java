package ru.yandex.practicum.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CartItemDto {
        private UUID productId;
        private String productName;
        private int quantity;
}

package ru.yandex.practicum.api;

import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.CartItemAddRequest;

import java.util.UUID;

public interface ShoppingCartApi {
    CartDto getCart(String username);
    CartDto addItem(String username, CartItemAddRequest request);
    CartDto changeQuantity(String username, UUID productId, int quantity);
    CartDto removeItem(String username, UUID productId);
    boolean deactivateCart(String username);
}
package ru.yandex.practicum.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingCartApi;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.cart.service.CartService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CartController implements ShoppingCartApi {
    private final CartService cartService;

    @Override
    public CartDto getCart(String username) {
        return cartService.getCart(username);
    }

    @Override
    public CartDto addItem(String username, UUID productId, int quantity) {
        return cartService.addItem(username, productId, quantity);
    }

    @Override
    public CartDto removeItem(String username, UUID productId) {
        return cartService.removeItem(username, productId);
    }

    @Override
    public boolean deactivateCart(String username) {
        return cartService.deactivateCart(username);
    }
}

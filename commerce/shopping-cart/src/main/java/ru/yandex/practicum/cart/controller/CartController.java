package ru.yandex.practicum.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.ShoppingCartApi;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.cart.service.CartService;

import java.util.*;

@RestController
@RequiredArgsConstructor
public class CartController implements ShoppingCartApi {

    private final CartService service;

    @Override
    @GetMapping("/api/v1/shopping-cart")
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {
        return service.getCart(username);
    }

    @Override
    @PutMapping("/api/v1/shopping-cart")
    public ShoppingCartDto addProductToShoppingCart(
            @RequestParam String username,
            @RequestBody Map<UUID, Long> products
    ) {
        return service.addProducts(username, products);
    }

    @Override
    @DeleteMapping("/api/v1/shopping-cart")
    public void deactivateCurrentShoppingCart(@RequestParam String username) {
        service.deactivateCart(username);
    }

    @Override
    @PostMapping("/api/v1/shopping-cart/remove")
    public ShoppingCartDto removeFromShoppingCart(
            @RequestParam String username,
            @RequestBody List<UUID> productIds
    ) {
        return service.removeProducts(username, productIds);
    }

    @Override
    @PostMapping("/api/v1/shopping-cart/change-quantity")
    public ShoppingCartDto changeProductQuantity(
            @RequestParam String username,
            @RequestBody ChangeProductQuantityRequest request
    ) {
        return service.changeQuantity(username, request);
    }
}
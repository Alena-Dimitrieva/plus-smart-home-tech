package ru.yandex.practicum.cart.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.ShoppingCartApi;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.CartItemAddRequest;
import ru.yandex.practicum.cart.service.CartService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController implements ShoppingCartApi {
    private final CartService cartService;

    @Override
    @GetMapping
    public CartDto getCart(@RequestParam String username) {
        return cartService.getCart(username);
    }

    @Override
    @PutMapping
    public CartDto addItem(@RequestParam String username, @RequestBody CartItemAddRequest request) {
        return cartService.addItem(username, request.getProductId(), request.getQuantity());
    }

    @Override
    @PostMapping("/change-quantity")
    public CartDto changeQuantity(@RequestParam String username, @RequestParam UUID productId, @RequestParam int quantity) {
        return cartService.changeQuantity(username, productId, quantity); // если такого метода нет – добавьте
    }

    @Override
    @PostMapping("/remove")
    public CartDto removeItem(@RequestParam String username, @RequestParam UUID productId) {
        return cartService.removeItem(username, productId);
    }

    @Override
    @DeleteMapping
    public boolean deactivateCart(@RequestParam String username) {
        return cartService.deactivateCart(username);
    }
}

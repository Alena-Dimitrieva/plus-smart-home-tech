package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.CartItemAddRequest;

import java.util.UUID;

@RestController
public interface ShoppingCartApi {

    @GetMapping("/api/v1/shopping-cart")
    CartDto getCart(@RequestParam String username);

    @PutMapping("/api/v1/shopping-cart")
    CartDto addItem(@RequestParam String username, @RequestBody CartItemAddRequest request);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    CartDto changeQuantity(@RequestParam String username, @RequestParam UUID productId, @RequestParam int quantity);

    CartDto addItem(String username, UUID productId, int quantity);

    @PostMapping("/api/v1/shopping-cart/remove")
    CartDto removeItem(@RequestParam String username, @RequestParam UUID productId);

    @DeleteMapping("/api/v1/shopping-cart")
    boolean deactivateCart(@RequestParam String username);
}
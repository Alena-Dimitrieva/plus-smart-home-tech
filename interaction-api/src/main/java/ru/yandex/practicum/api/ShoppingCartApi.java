package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CartDto;

import java.util.UUID;

public interface ShoppingCartApi {

    @GetMapping("/api/v1/shopping-cart/{username}")
    CartDto getCart(@PathVariable String username);

    @PostMapping("/api/v1/shopping-cart/{username}/add")
    CartDto addItem(@PathVariable String username, @RequestParam UUID productId, @RequestParam int quantity);

    @PostMapping("/api/v1/shopping-cart/{username}/remove")
    CartDto removeItem(@PathVariable String username, @RequestParam UUID productId);

    @PostMapping("/api/v1/shopping-cart/{username}/deactivate")
    boolean deactivateCart(@PathVariable String username);
}
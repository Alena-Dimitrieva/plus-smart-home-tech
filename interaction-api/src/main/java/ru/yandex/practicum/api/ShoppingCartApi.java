package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CartDto;

import java.util.UUID;

@RequestMapping("/api/v1/shopping-cart")
public interface ShoppingCartApi {

    @GetMapping("/{username}")
    CartDto getCart(@PathVariable String username);

    @PostMapping("/{username}/add")
    CartDto addItem(@PathVariable String username, @RequestParam UUID productId, @RequestParam int quantity);

    @PostMapping("/{username}/remove")
    CartDto removeItem(@PathVariable String username, @RequestParam UUID productId);

    @PostMapping("/{username}/deactivate")
    boolean deactivateCart(@PathVariable String username);
}

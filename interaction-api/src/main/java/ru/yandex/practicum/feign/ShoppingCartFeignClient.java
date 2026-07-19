package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.CartItemAddRequest;

import java.util.UUID;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartFeignClient {

    @GetMapping("/api/v1/shopping-cart")
    CartDto getCart(@RequestParam String username);

    @PutMapping("/api/v1/shopping-cart")
    CartDto addItem(@RequestParam String username, @RequestBody CartItemAddRequest request);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    CartDto changeQuantity(@RequestParam String username, @RequestParam UUID productId, @RequestParam int quantity);

    @PostMapping("/api/v1/shopping-cart/remove")
    CartDto removeItem(@RequestParam String username, @RequestParam UUID productId);

    @DeleteMapping("/api/v1/shopping-cart")
    boolean deactivateCart(@RequestParam String username);
}

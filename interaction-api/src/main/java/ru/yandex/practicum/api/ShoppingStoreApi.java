package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.ProductCategory;
import java.util.UUID;

public interface ShoppingStoreApi {
    @GetMapping("/api/v1/shopping-store")
    PageProductDto getProducts(@RequestParam ProductCategory category,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "productName,asc") String[] sort);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    boolean setProductQuantityState(@RequestBody SetProductQuantityStateRequest request);
}
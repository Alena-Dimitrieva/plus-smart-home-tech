package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.model.ProductCategory;

import java.util.UUID;

@RequestMapping("/api/v1/shopping-store")
public interface ShoppingStoreApi {

    @GetMapping
    PageProductDto getProducts(
            @RequestParam("category") ProductCategory category,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "productName,asc") String[] sort
    );

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);

    @PutMapping
    ProductDto createNewProduct(@RequestBody ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@RequestBody ProductDto productDto);

    @PostMapping("/removeProductFromStore")
    boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/quantityState")
    void setProductQuantityState(@RequestBody SetProductQuantityStateRequest request);
}

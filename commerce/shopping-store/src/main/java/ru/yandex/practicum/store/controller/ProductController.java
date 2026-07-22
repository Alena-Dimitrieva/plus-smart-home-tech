package ru.yandex.practicum.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.QuantityState;
import ru.yandex.practicum.store.service.ProductService;
import ru.yandex.practicum.store.util.SortUtils;


import java.util.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductController implements ShoppingStoreApi {

    private final ProductService service;
    private final SortUtils sortUtils;

    @Override
    @GetMapping("/api/v1/shopping-store")
    public PageProductDto getProducts(
            @RequestParam ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productName,asc") String[] sort
    ) {
        return service.getProducts(category, PageRequest.of(page, size, sortUtils.parseSort(sort)));
    }

    @Override
    @GetMapping("/api/v1/shopping-store/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return service.getProduct(productId);
    }

    @Override
    @PutMapping("/api/v1/shopping-store")
    public ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto) {
        log.info("Создание товара с переданным ID: {}", productDto.getProductId());
        return service.createProduct(productDto);
    }

    @Override
    @PostMapping("/api/v1/shopping-store")
    public ProductDto updateProduct(@Valid @RequestBody ProductDto productDto) {
        return service.updateProduct(productDto);
    }

    @Override
    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must be provided");
        }
        return service.removeProductFromStore(productId);
    }

    @Override
    @PostMapping("/api/v1/shopping-store/quantityState")
    public boolean setProductQuantityState(
            @RequestParam UUID productId,
            @RequestParam QuantityState quantityState
    ) {
        log.info("Запрос на обновление статуса для ID: {}, новый статус: {}", productId, quantityState);
        SetProductQuantityStateRequest request = new SetProductQuantityStateRequest();
        request.setProductId(productId);
        request.setQuantityState(quantityState);
        return service.setProductQuantityState(request);
    }
}
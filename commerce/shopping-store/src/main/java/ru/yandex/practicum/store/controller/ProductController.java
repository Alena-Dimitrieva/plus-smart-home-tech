package ru.yandex.practicum.store.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.store.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductController implements ShoppingStoreApi {

    private final ProductService service;

    @Override
    @GetMapping("/api/v1/shopping-store")
    public PageProductDto getProducts(
            @RequestParam ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productName,asc") String[] sort
    ) {
        Sort sortObj = parseSort(sort);
        return service.getProducts(category, PageRequest.of(page, size, sortObj));
    }

    @Override
    @GetMapping("/api/v1/shopping-store/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return service.getProduct(productId);
    }

    @Override
    @PutMapping("/api/v1/shopping-store")
    public ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto) {
        return service.createProduct(productDto);
    }

    @Override
    @PostMapping("/api/v1/shopping-store")
    public ProductDto updateProduct(@RequestBody @Valid ProductDto productDto) {
        return service.updateProduct(productDto);
    }

    @Override
    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody UUID productId) {
        return service.removeProductFromStore(productId);
    }

    @Override
    @PostMapping("/api/v1/shopping-store/quantityState")
    public boolean setProductQuantityState(@RequestBody @Valid SetProductQuantityStateRequest request) {
        return service.setProductQuantityState(request);
    }

    private Sort parseSort(String[] sortArray) {
        if (sortArray == null || sortArray.length == 0) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sortArray) {
            String[] parts = s.split(",");
            String property = parts[0].trim();
            if (property.isEmpty()) continue;
            Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, property));
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}
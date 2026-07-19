package ru.yandex.practicum.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.ShoppingStoreApi;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.store.service.ProductService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductController implements ShoppingStoreApi {
    private final ProductService productService;

    @Override
    public PageProductDto getProducts(ProductCategory category, int page, int size, String[] sort) {
        return productService.getProducts(category, PageRequest.of(page, size, parseSort(sort)));
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return productService.getProduct(productId);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        return productService.createProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return productService.updateProduct(productDto);
    }

    @Override
    public boolean removeProductFromStore(UUID productId) {
        return productService.removeProductFromStore(productId);
    }

    @Override
    public void setProductQuantityState(SetProductQuantityStateRequest request) {
        productService.setProductQuantityState(request);
    }

    private Sort parseSort(String[] sortArray) {
        List<Sort.Order> orders = new ArrayList<>();
        for (String s : sortArray) {
            String[] parts = s.split(",");
            String property = parts[0];
            Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1])) ? Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, property));
        }
        return Sort.by(orders);
    }
}

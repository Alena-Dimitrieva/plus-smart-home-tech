package ru.yandex.practicum.api;

import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.QuantityState;

import java.util.UUID;

public interface ShoppingStoreApi {
    PageProductDto getProducts(ProductCategory category, int page, int size, String[] sort);
    ProductDto getProduct(UUID productId);
    ProductDto createNewProduct(ProductDto productDto);
    ProductDto updateProduct(ProductDto productDto);
    boolean removeProductFromStore(UUID productId);
    boolean setProductQuantityState(UUID productId, QuantityState quantityState);
}
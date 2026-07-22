package ru.yandex.practicum.dto;

public class ProductInShoppingCartLowQuantityInWarehouse extends RuntimeException {
    public ProductInShoppingCartLowQuantityInWarehouse() {
    }

    public ProductInShoppingCartLowQuantityInWarehouse(String message) {
        super(message);
    }
}
package ru.yandex.practicum.dto;

public class NoProductsInShoppingCartException extends RuntimeException {
    public NoProductsInShoppingCartException() {
    }

    public NoProductsInShoppingCartException(String message) {
        super(message);
    }
}
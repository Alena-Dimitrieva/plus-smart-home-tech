package ru.yandex.practicum.dto;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {
    public NoSpecifiedProductInWarehouseException() {
    }

    public NoSpecifiedProductInWarehouseException(String message) {
        super(message);
    }
}
package ru.yandex.practicum.dto;

public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {
    public SpecifiedProductAlreadyInWarehouseException() {
    }

    public SpecifiedProductAlreadyInWarehouseException(String message) {
        super(message);
    }
}
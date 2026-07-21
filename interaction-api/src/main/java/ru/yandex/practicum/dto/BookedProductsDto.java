package ru.yandex.practicum.dto;

import lombok.Data;

@Data
public class BookedProductsDto {
    private double deliveryWeight;
    private double deliveryVolume;
    private boolean fragile;
}

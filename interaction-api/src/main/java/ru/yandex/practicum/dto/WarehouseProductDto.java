package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class WarehouseProductDto {
    private UUID productId;
    private int quantity;
    private double width;
    private double height;
    private double depth;
    private double weight;
    private boolean fragile;
}

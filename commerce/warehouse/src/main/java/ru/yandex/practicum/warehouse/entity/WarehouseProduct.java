package ru.yandex.practicum.warehouse.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Data
public class WarehouseProduct {
    @Id
    private UUID productId;
    private int quantity;
    private double width;
    private double height;
    private double depth;
    private double weight;
    private boolean fragile;
}
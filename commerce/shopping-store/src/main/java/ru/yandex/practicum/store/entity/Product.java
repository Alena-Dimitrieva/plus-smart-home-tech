package ru.yandex.practicum.store.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.model.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productId;

    private String productName;
    private String description;
    private String imageSrc;

    @Enumerated(EnumType.STRING)
    private QuantityState quantityState;

    @Enumerated(EnumType.STRING)
    private ProductState productState;

    @Enumerated(EnumType.STRING)
    private ProductCategory productCategory;

    private BigDecimal price;
}

package ru.yandex.practicum.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.model.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductDto {
    private UUID productId;

    @NotBlank(message = "productName обязателен")
    private String productName;

    @NotBlank(message = "description обязателен")
    private String description;

    private String imageSrc;

    @NotNull(message = "quantityState обязателен")
    private QuantityState quantityState;

    @NotNull(message = "productState обязателен")
    private ProductState productState;

    private ProductCategory productCategory;

    @NotNull(message = "price обязателен")
    @DecimalMin(value = "0.01", message = "price должен быть >= 0.01")
    private BigDecimal price;
}
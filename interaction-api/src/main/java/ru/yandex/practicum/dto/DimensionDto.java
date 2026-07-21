package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class DimensionDto {
    @Min(value = 1, message = "width должна быть > 0")
    private double width;

    @Min(value = 1, message = "height должна быть > 0")
    private double height;

    @Min(value = 1, message = "depth должна быть > 0")
    private double depth;
}
package ru.yandex.practicum.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DimensionDto {
    @NotNull
    @Min(value = 1, message = "width должна быть > 0")
    private double width;

    @NotNull
    @Min(value = 1, message = "height должна быть > 0")
    private double height;

    @NotNull
    @Min(value = 1, message = "depth должна быть > 0")
    private double depth;
}
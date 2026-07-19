package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartDto {
    private Long id;
    private String username;
    private List<CartItemDto> items;
    private boolean active;
}

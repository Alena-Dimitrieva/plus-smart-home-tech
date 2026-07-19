package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageProductDto {
    private List<ProductDto> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;
    private boolean first;
    private boolean last;
    private boolean empty;
}

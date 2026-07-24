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
    private List<SortObject> sort;
    private int numberOfElements;
    private PageableObject pageable;

    @Data
    public static class PageableObject {
        private int pageNumber;
        private int pageSize;
        private long offset;
        private List<SortObject> sort;
        private boolean paged;
        private boolean unpaged;
    }

    @Data
    public static class SortObject {
        private String direction;
        private String property;
        private boolean ascending;
        private String nullHandling;
        private boolean ignoreCase;
    }
}
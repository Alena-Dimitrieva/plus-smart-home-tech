package ru.yandex.practicum.store.mapper;

import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.store.entity.Product;
import org.springframework.data.domain.Page;

import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setImageSrc(product.getImageSrc());
        dto.setQuantityState(product.getQuantityState());
        dto.setProductState(product.getProductState());
        dto.setProductCategory(product.getProductCategory());
        dto.setPrice(product.getPrice());
        return dto;
    }

    public static Product toEntity(ProductDto dto) {
        Product product = new Product();
        product.setProductId(dto.getProductId());
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setQuantityState(dto.getQuantityState());
        product.setProductState(dto.getProductState() != null ? dto.getProductState() : ProductState.ACTIVE);
        product.setProductCategory(dto.getProductCategory());
        product.setPrice(dto.getPrice());
        return product;
    }

    public static PageProductDto toPageDto(Page<Product> page) {
        PageProductDto dto = new PageProductDto();
        dto.setContent(page.getContent().stream().map(ProductMapper::toDto).collect(Collectors.toList()));
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        dto.setNumber(page.getNumber());
        dto.setSize(page.getSize());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        dto.setEmpty(page.isEmpty());
        dto.setNumberOfElements(page.getNumberOfElements());

        dto.setSort(page.getSort().stream()
                .map(order -> {
                    PageProductDto.SortObject sortObj = new PageProductDto.SortObject();
                    sortObj.setProperty(order.getProperty());
                    sortObj.setDirection(order.getDirection().name());
                    sortObj.setAscending(order.isAscending());
                    sortObj.setIgnoreCase(order.isIgnoreCase());
                    sortObj.setNullHandling(order.getNullHandling().name());
                    return sortObj;
                }).collect(Collectors.toList()));

        PageProductDto.PageableObject pageableObj = new PageProductDto.PageableObject();
        pageableObj.setPageNumber(page.getNumber());
        pageableObj.setPageSize(page.getSize());
        pageableObj.setOffset(page.getPageable().getOffset());
        pageableObj.setSort(dto.getSort());
        pageableObj.setPaged(page.getPageable().isPaged());
        pageableObj.setUnpaged(page.getPageable().isUnpaged());
        dto.setPageable(pageableObj);

        return dto;
    }
}
package ru.yandex.practicum.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.store.entity.Product;
import ru.yandex.practicum.store.repository.ProductRepository;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;

    public PageProductDto getProducts(ProductCategory category, Pageable pageable) {
        Page<Product> page = repository.findByProductCategoryAndProductState(category, ProductState.ACTIVE, pageable);
        PageProductDto dto = new PageProductDto();
        dto.setContent(page.getContent().stream().map(this::toDto).collect(Collectors.toList()));
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        dto.setNumber(page.getNumber());
        dto.setSize(page.getSize());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        dto.setEmpty(page.isEmpty());
        return dto;
    }

    public ProductDto getProduct(UUID id) {
        Product p = repository.findById(id).orElseThrow(ProductNotFoundException::new);
        return toDto(p);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product p = new Product();
        p.setProductName(dto.getProductName());
        p.setDescription(dto.getDescription());
        p.setImageSrc(dto.getImageSrc());
        p.setQuantityState(dto.getQuantityState());
        p.setProductState(ProductState.ACTIVE);
        p.setProductCategory(dto.getProductCategory());
        p.setPrice(dto.getPrice());
        return toDto(repository.save(p));
    }

    @Transactional
    public ProductDto updateProduct(ProductDto dto) {
        Product existing = repository.findById(dto.getProductId()).orElseThrow(ProductNotFoundException::new);
        existing.setProductName(dto.getProductName());
        existing.setDescription(dto.getDescription());
        existing.setImageSrc(dto.getImageSrc());
        existing.setProductCategory(dto.getProductCategory());
        existing.setPrice(dto.getPrice());
        return toDto(repository.save(existing));
    }

    @Transactional
    public boolean removeProductFromStore(UUID id) {
        Product p = repository.findById(id).orElseThrow(ProductNotFoundException::new);
        p.setProductState(ProductState.DEACTIVATE);
        repository.save(p);
        return true;
    }

    @Transactional
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = repository.findById(request.getProductId())
                .orElseThrow(ProductNotFoundException::new);
        product.setQuantityState(request.getQuantityState());
        repository.save(product);
        return true;
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setDescription(p.getDescription());
        dto.setImageSrc(p.getImageSrc());
        dto.setQuantityState(p.getQuantityState());
        dto.setProductState(p.getProductState());
        dto.setProductCategory(p.getProductCategory());
        dto.setPrice(p.getPrice());
        return dto;
    }
}
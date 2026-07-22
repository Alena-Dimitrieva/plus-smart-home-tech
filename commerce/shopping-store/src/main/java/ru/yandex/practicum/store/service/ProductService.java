package ru.yandex.practicum.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        Product p = repository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return toDto(p);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        UUID productId = dto.getProductId();
        if (productId == null) {
            productId = UUID.randomUUID();
        } else if (repository.existsById(productId)) {
            throw new IllegalArgumentException("Product with this ID already exists: " + productId);
        }
        Product p = Product.builder()
                .productId(productId)
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .imageSrc(dto.getImageSrc())
                .quantityState(dto.getQuantityState())
                .productState(ProductState.ACTIVE)
                .productCategory(dto.getProductCategory())
                .price(dto.getPrice())
                .build();
        return toDto(repository.save(p));
    }

    @Transactional
    public ProductDto updateProduct(ProductDto dto) {
        if (dto.getProductId() == null) {
            throw new ProductNotFoundException("Product ID must not be null for update");
        }
        Product existing = repository.findById(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + dto.getProductId()));
        existing.setProductName(dto.getProductName());
        existing.setDescription(dto.getDescription());
        existing.setImageSrc(dto.getImageSrc());
        existing.setQuantityState(dto.getQuantityState());
        existing.setProductState(dto.getProductState());
        existing.setProductCategory(dto.getProductCategory());
        existing.setPrice(dto.getPrice());
        return toDto(repository.save(existing));
    }

    @Transactional
    public boolean removeProductFromStore(UUID id) {
        Product p = repository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        p.setProductState(ProductState.DEACTIVATE);
        repository.save(p);
        log.info("Товар {} деактивирован", id);
        return true;
    }

    @Transactional
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        log.info("Обновление статуса для ID: {}", request.getProductId());
        Product product = repository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + request.getProductId()));
        product.setQuantityState(request.getQuantityState());
        repository.saveAndFlush(product);
        log.info("Статус обновлён для ID: {}", request.getProductId());
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
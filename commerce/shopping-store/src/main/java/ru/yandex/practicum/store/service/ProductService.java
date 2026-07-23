package ru.yandex.practicum.store.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.store.entity.Product;
import ru.yandex.practicum.store.exception.ProductNotFoundException;
import ru.yandex.practicum.store.mapper.ProductMapper;
import ru.yandex.practicum.store.repository.ProductRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository repository;

    public PageProductDto getProducts(ProductCategory category, Pageable pageable) {
        Page<Product> page = repository.findByProductCategoryAndProductState(category, ru.yandex.practicum.model.ProductState.ACTIVE, pageable);
        return ProductMapper.toPageDto(page);
    }

    public ProductDto getProduct(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        return ProductMapper.toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        UUID productId = dto.getProductId();
        if (productId == null) {
            productId = UUID.randomUUID();
        } else if (repository.existsById(productId)) {
            throw new IllegalArgumentException("Product with this ID already exists: " + productId);
        }
        Product product = ProductMapper.toEntity(dto);
        product.setProductId(productId);
        return ProductMapper.toDto(repository.save(product));
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
        return ProductMapper.toDto(existing);
    }

    @Transactional
    public boolean removeProductFromStore(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        product.setProductState(ru.yandex.practicum.model.ProductState.DEACTIVATE);
        repository.save(product);
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
}
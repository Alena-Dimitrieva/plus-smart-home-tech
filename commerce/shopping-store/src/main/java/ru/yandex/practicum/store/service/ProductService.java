package ru.yandex.practicum.store.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductDto;
import ru.yandex.practicum.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.dto.PageProductDto;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.model.QuantityState;
import ru.yandex.practicum.store.entity.Product;
import ru.yandex.practicum.store.exception.NotFoundException;
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
        Product product = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        return toDto(product);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setImageSrc(dto.getImageSrc());
        product.setQuantityState(dto.getQuantityState() != null ? dto.getQuantityState() : QuantityState.ENDED);
        product.setProductState(ProductState.ACTIVE);
        product.setProductCategory(dto.getProductCategory());
        product.setPrice(dto.getPrice());
        return toDto(repository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(ProductDto dto) {
        if (dto.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required for update");
        }
        Product existing = repository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + dto.getProductId()));
        existing.setProductName(dto.getProductName());
        existing.setDescription(dto.getDescription());
        existing.setImageSrc(dto.getImageSrc());
        existing.setProductCategory(dto.getProductCategory());
        existing.setPrice(dto.getPrice());
        return toDto(repository.save(existing));
    }

    @Transactional
    public boolean removeProductFromStore(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
        product.setProductState(ProductState.DEACTIVATE);
        repository.save(product);
        return true;
    }

    @Transactional
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = repository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found: " + request.getProductId()));
        product.setQuantityState(request.getQuantityState());
        repository.save(product);
        return true;
    }

    private ProductDto toDto(Product product) {
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
}
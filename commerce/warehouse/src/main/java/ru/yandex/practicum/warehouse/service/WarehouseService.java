package ru.yandex.practicum.warehouse.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.ShoppingStoreFeignClient;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.model.QuantityState;
import ru.yandex.practicum.warehouse.entity.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {
    private final WarehouseProductRepository repository;
    private final ShoppingStoreFeignClient storeClient;

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private final String currentAddress = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    public AddressDto getWarehouseAddress() {
        AddressDto dto = new AddressDto();
        dto.setCountry(currentAddress);
        dto.setCity(currentAddress);
        dto.setStreet(currentAddress);
        dto.setHouse(currentAddress);
        dto.setFlat(currentAddress);
        return dto;
    }

    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (repository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException("Product already exists in warehouse");
        }

        WarehouseProduct wp = new WarehouseProduct();
        wp.setProductId(request.getProductId());
        wp.setQuantity(0);
        wp.setWidth(request.getDimension().getWidth());
        wp.setHeight(request.getDimension().getHeight());
        wp.setDepth(request.getDimension().getDepth());
        wp.setWeight(request.getWeight());
        wp.setFragile(request.getFragile() != null && request.getFragile());
        repository.save(wp);

        updateQuantityState(request.getProductId());
    }

    @Transactional
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        if (request.getProductId() == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        WarehouseProduct wp = repository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Product not found in warehouse"));
        wp.setQuantity((int) (wp.getQuantity() + request.getQuantity()));
        repository.save(wp);

        updateQuantityState(request.getProductId());
    }

    public BookedProductsDto checkAvailability(ShoppingCartDto cart) {
        if (cart.getProducts() == null) {
            throw new IllegalArgumentException("Shopping cart products must not be null");
        }
        double totalWeight = 0, totalVolume = 0;
        boolean hasFragile = false;
        for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
            UUID pid = entry.getKey();
            long requested = entry.getValue();
            WarehouseProduct wp = repository.findById(pid)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Product not found: " + pid));
            if (wp.getQuantity() < requested) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Not enough quantity for product " + pid + ". Available: " + wp.getQuantity() + ", requested: " + requested
                );
            }
            totalWeight += wp.getWeight() * requested;
            totalVolume += wp.getWidth() * wp.getHeight() * wp.getDepth() * requested;
            if (wp.isFragile()) hasFragile = true;
        }
        BookedProductsDto dto = new BookedProductsDto();
        dto.setDeliveryWeight(totalWeight);
        dto.setDeliveryVolume(totalVolume);
        dto.setFragile(hasFragile);
        return dto;
    }

    private void updateQuantityState(UUID productId) {
        WarehouseProduct wp = repository.findById(productId)
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException("Product not found"));
        long q = wp.getQuantity();
        QuantityState state = determineState(q);

        try {
            storeClient.setProductQuantityState(productId, state);
        } catch (FeignException.NotFound e) {
            // Товара нет в витрине – создаём его автоматически
            log.warn("Товар {} не найден в витрине. Создаём автоматически.", productId);
            createProductInStore(productId, state);
            // Повторно обновляем статус (теперь товар существует)
            storeClient.setProductQuantityState(productId, state);
        } catch (FeignException e) {
            log.error("Ошибка при обновлении статуса в витрине: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обновить статус товара в витрине", e);
        }

        log.info("Обновлён статус количества для товара {}: {}", productId, state);
    }

    private QuantityState determineState(long q) {
        if (q == 0) return QuantityState.ENDED;
        if (q < 10) return QuantityState.FEW;
        if (q <= 100) return QuantityState.ENOUGH;
        return QuantityState.MANY;
    }

    private void createProductInStore(UUID productId, QuantityState state) {
        ProductDto productDto = new ProductDto();
        productDto.setProductId(productId);
        productDto.setProductName("Auto-created product: " + productId);
        productDto.setDescription("Created automatically by warehouse");
        productDto.setQuantityState(state);
        productDto.setProductState(ProductState.ACTIVE);
        productDto.setProductCategory(ProductCategory.CONTROL); // категория по умолчанию
        productDto.setPrice(BigDecimal.ONE);
        storeClient.createNewProduct(productDto);
        log.info("Товар {} создан в витрине", productId);
    }
}
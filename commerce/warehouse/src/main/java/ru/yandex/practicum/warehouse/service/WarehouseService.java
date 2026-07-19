package ru.yandex.practicum.warehouse.service;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.ShoppingStoreFeignClient;
import ru.yandex.practicum.model.QuantityState;
import ru.yandex.practicum.warehouse.entity.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseProductRepository repository;
    private final ShoppingStoreFeignClient storeClient;

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private final String currentAddress = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    public AddressDto getWarehouseAddress() {
        AddressDto address = new AddressDto();
        address.setCountry(currentAddress);
        address.setCity(currentAddress);
        address.setStreet(currentAddress);
        address.setHouse(currentAddress);
        address.setFlat(currentAddress);
        return address;
    }

    public AvailabilityResponse checkAvailability(CartDto cart) {
        AvailabilityResponse response = new AvailabilityResponse();
        List<UUID> missing = new ArrayList<>();
        boolean allAvailable = true;
        for (CartItemDto item : cart.getItems()) {
            Optional<WarehouseProduct> opt = repository.findById(item.getProductId());
            if (opt.isEmpty() || opt.get().getQuantity() < item.getQuantity()) {
                missing.add(item.getProductId());
                allAvailable = false;
            }
        }
        response.setAvailable(allAvailable);
        response.setMissingProductIds(missing);
        return response;
    }

    public void addProduct(WarehouseProductDto dto) {
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(dto.getProductId());
        product.setQuantity(dto.getQuantity());
        product.setWidth(dto.getWidth());
        product.setHeight(dto.getHeight());
        product.setDepth(dto.getDepth());
        product.setWeight(dto.getWeight());
        product.setFragile(dto.isFragile());
        repository.save(product);
        updateQuantityState(dto.getProductId());
    }

    public void changeQuantity(UUID productId, int delta) {
        WarehouseProduct product = repository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        int newQuantity = product.getQuantity() + delta;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        product.setQuantity(newQuantity);
        repository.save(product);
        updateQuantityState(productId);
    }

    private void updateQuantityState(UUID productId) {
        WarehouseProduct product = repository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
        int q = product.getQuantity();
        QuantityState state;
        if (q == 0) state = QuantityState.ENDED;
        else if (q < 10) state = QuantityState.FEW;
        else if (q <= 100) state = QuantityState.ENOUGH;
        else state = QuantityState.MANY;

        storeClient.setProductQuantityState(productId, state);
    }
}

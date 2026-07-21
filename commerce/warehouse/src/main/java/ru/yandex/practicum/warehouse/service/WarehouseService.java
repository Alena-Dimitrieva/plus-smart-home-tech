package ru.yandex.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.ShoppingStoreFeignClient;
import ru.yandex.practicum.model.QuantityState;
import ru.yandex.practicum.warehouse.entity.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;
import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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
        if (repository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException();
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
        WarehouseProduct wp = repository.findById(request.getProductId())
                .orElseThrow(NoSpecifiedProductInWarehouseException::new);
        wp.setQuantity((int) (wp.getQuantity() + request.getQuantity()));
        repository.save(wp);
        updateQuantityState(request.getProductId());
    }

    public BookedProductsDto checkAvailability(ShoppingCartDto cart) {
        double totalWeight = 0, totalVolume = 0;
        boolean hasFragile = false;
        for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
            UUID pid = entry.getKey();
            long requested = entry.getValue();
            WarehouseProduct wp = repository.findById(pid)
                    .orElseThrow(NoSpecifiedProductInWarehouseException::new);
            if (wp.getQuantity() < requested) {
                throw new ProductInShoppingCartLowQuantityInWarehouse();
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
        WarehouseProduct wp = repository.findById(productId).orElseThrow();
        long q = wp.getQuantity();
        QuantityState state;
        if (q == 0) state = QuantityState.ENDED;
        else if (q < 10) state = QuantityState.FEW;
        else if (q <= 100) state = QuantityState.ENOUGH;
        else state = QuantityState.MANY;
        SetProductQuantityStateRequest req = new SetProductQuantityStateRequest();
        req.setProductId(productId);
        req.setQuantityState(state);
        storeClient.setProductQuantityState(req);
    }
}
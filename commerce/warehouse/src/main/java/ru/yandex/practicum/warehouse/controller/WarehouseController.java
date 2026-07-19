package ru.yandex.practicum.warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.api.WarehouseApi;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AvailabilityResponse;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.WarehouseProductDto;
import ru.yandex.practicum.warehouse.service.WarehouseService;

@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {
    private final WarehouseService warehouseService;

    @Override
    public AvailabilityResponse checkAvailability(CartDto cart) {
        return warehouseService.checkAvailability(cart);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    public WarehouseProductDto addProduct(WarehouseProductDto product) {
        return null;
    }

    @Override
    public WarehouseProductDto addProductAlternative(WarehouseProductDto product) {
        return null;
    }
}

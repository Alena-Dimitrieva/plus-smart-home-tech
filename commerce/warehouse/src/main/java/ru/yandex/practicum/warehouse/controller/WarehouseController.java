package ru.yandex.practicum.warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.WarehouseApi;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AvailabilityResponse;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.WarehouseProductDto;
import ru.yandex.practicum.warehouse.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {
    private final WarehouseService warehouseService;

    @Override
    @PostMapping("/check")
    public AvailabilityResponse checkAvailability(@RequestBody CartDto cart) {
        return warehouseService.checkAvailability(cart);
    }

    @Override
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    @PutMapping
    public WarehouseProductDto addProduct(@RequestBody WarehouseProductDto product) {
        warehouseService.addProduct(product);
        return product;
    }

    @Override
    @PostMapping("/add")
    public WarehouseProductDto addProductAlternative(@RequestBody WarehouseProductDto product) {
        warehouseService.addProduct(product);
        return product;
    }
}

package ru.yandex.practicum.warehouse.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.WarehouseApi;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {

    private final WarehouseService service;

    @Override
    @PutMapping("/api/v1/warehouse")
    public void newProductInWarehouse(@RequestBody @Valid NewProductInWarehouseRequest request) {
        service.addNewProduct(request);
    }

    @Override
    @PostMapping("/api/v1/warehouse/check")
    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(@RequestBody @Valid ShoppingCartDto cart) {
        return service.checkAvailability(cart);
    }

    @Override
    @PostMapping("/api/v1/warehouse/add")
    public void addProductToWarehouse(@RequestBody @Valid AddProductToWarehouseRequest request) {
        service.addProductQuantity(request);
    }

    @Override
    @GetMapping("/api/v1/warehouse/address")
    public AddressDto getWarehouseAddress() {
        return service.getWarehouseAddress();
    }

    @Override
    @PostMapping("/api/v1/warehouse/assembly")
    public BookedProductsDto assemblyProductsForOrder(@RequestBody @Valid AssemblyProductsForOrderRequest request) {
        return service.assemblyProductsForOrder(request);
    }

    @Override
    @PostMapping("/api/v1/warehouse/shipped")
    public void shippedToDelivery(@RequestBody @Valid ShippedToDeliveryRequest request) {
        service.shippedToDelivery(request);
    }

    @Override
    @PostMapping("/api/v1/warehouse/return")
    public void acceptReturn(@RequestBody Map<UUID, Long> products) {
        service.acceptReturn(products);
    }
}
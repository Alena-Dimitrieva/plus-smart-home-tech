package ru.yandex.practicum.api;

import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AvailabilityResponse;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.WarehouseProductDto;

public interface WarehouseApi {
    AvailabilityResponse checkAvailability(CartDto cart);
    AddressDto getWarehouseAddress();
    WarehouseProductDto addProduct(WarehouseProductDto product);
    WarehouseProductDto addProductAlternative(WarehouseProductDto product);
}
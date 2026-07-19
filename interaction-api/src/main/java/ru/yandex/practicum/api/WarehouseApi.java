package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AvailabilityResponse;
import ru.yandex.practicum.dto.CartDto;

public interface WarehouseApi {

    @PostMapping("/api/v1/warehouse/check")
    AvailabilityResponse checkAvailability(@RequestBody CartDto cart);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();
}
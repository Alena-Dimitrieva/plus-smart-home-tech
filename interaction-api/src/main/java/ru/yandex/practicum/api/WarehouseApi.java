package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AvailabilityResponse;
import ru.yandex.practicum.dto.CartDto;

@RequestMapping("/api/v1/warehouse")
public interface WarehouseApi {

    @PostMapping("/check")
    AvailabilityResponse checkAvailability(@RequestBody CartDto cart);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();
}
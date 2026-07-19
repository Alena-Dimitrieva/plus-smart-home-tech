package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.AvailabilityResponse;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.WarehouseProductDto;

@FeignClient(name = "warehouse")
public interface WarehouseFeignClient {

    @PostMapping("/api/v1/warehouse/check")
    AvailabilityResponse checkAvailability(@RequestBody CartDto cart);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();

    @PutMapping("/api/v1/warehouse")
    WarehouseProductDto addProduct(@RequestBody WarehouseProductDto product);

    @PostMapping("/api/v1/warehouse/add")
    WarehouseProductDto addProductAlternative(@RequestBody WarehouseProductDto product);
}
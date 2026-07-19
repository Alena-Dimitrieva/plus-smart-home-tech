package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.WarehouseApi;

@FeignClient(name = "warehouse")
public interface WarehouseFeignClient extends WarehouseApi {
}

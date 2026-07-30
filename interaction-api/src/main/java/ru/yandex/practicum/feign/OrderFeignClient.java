package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.OrderApi;

@FeignClient(name = "order")
public interface OrderFeignClient extends OrderApi {
}

package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.DeliveryApi;

@FeignClient(name = "delivery")
public interface DeliveryFeignClient extends DeliveryApi {
}
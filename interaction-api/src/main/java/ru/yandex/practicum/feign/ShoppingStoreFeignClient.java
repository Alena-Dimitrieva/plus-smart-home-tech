package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.ShoppingStoreApi;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreFeignClient extends ShoppingStoreApi {
}
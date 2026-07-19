package ru.yandex.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.api.ShoppingCartApi;

@FeignClient(name = "shopping-cart")
public interface ShoppingCartFeignClient extends ShoppingCartApi {
}

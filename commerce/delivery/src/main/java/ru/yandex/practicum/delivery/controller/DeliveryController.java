package ru.yandex.practicum.delivery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.DeliveryApi;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.delivery.service.DeliveryService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DeliveryController implements DeliveryApi {
    private final DeliveryService deliveryService;

    @Override
    @PutMapping("/api/v1/delivery")
    public DeliveryDto planDelivery(@RequestBody DeliveryDto delivery) {
        return deliveryService.planDelivery(delivery);
    }

    @Override
    @PostMapping("/api/v1/delivery/cost")
    public double deliveryCost(@RequestBody OrderDto order) {
        return deliveryService.deliveryCost(order);
    }

    @Override
    @PostMapping("/api/v1/delivery/picked")
    public void deliveryPicked(@RequestBody UUID orderId) {
        deliveryService.deliveryPicked(orderId);
    }

    @Override
    @PostMapping("/api/v1/delivery/successful")
    public void deliverySuccessful(@RequestBody UUID orderId) {
        deliveryService.deliverySuccessful(orderId);
    }

    @Override
    @PostMapping("/api/v1/delivery/failed")
    public void deliveryFailed(@RequestBody UUID orderId) {
        deliveryService.deliveryFailed(orderId);
    }
}
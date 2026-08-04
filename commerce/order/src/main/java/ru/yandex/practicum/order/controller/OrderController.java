package ru.yandex.practicum.order.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.api.OrderApi;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.order.service.OrderServiceImpl;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {
    private final OrderServiceImpl orderService;

    @Override
    @GetMapping("/api/v1/order")
    public List<OrderDto> getClientOrders(@RequestParam String username) {
        return orderService.getClientOrders(username);
    }

    @Override
    @PutMapping("/api/v1/order")
    public OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request,
                                   @RequestParam String username) {
        return orderService.createNewOrder(request, username);
    }

    @Override
    @PostMapping("/api/v1/order/payment")
    public OrderDto payment(@RequestBody UUID orderId) {
        return orderService.payment(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/payment/success")
    public void paymentSuccess(@RequestBody UUID orderId) {
        orderService.paymentSuccess(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/payment/failed")
    public void paymentFailed(@RequestBody UUID orderId) {
        orderService.paymentFailed(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/assembly")
    public OrderDto assembly(@RequestBody UUID orderId) {
        return orderService.assembly(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/assembly/failed")
    public void assemblyFailed(@RequestBody UUID orderId) {
        orderService.assemblyFailed(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/delivery")
    public OrderDto delivery(@RequestBody UUID orderId) {
        return orderService.delivery(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/delivery/success")
    public void deliverySuccess(@RequestBody UUID orderId) {
        orderService.deliverySuccess(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/delivery/failed")
    public void deliveryFailed(@RequestBody UUID orderId) {
        orderService.deliveryFailed(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/completed")
    public OrderDto complete(@RequestBody UUID orderId) {
        return orderService.complete(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/calculate/total")
    public OrderDto calculateTotalCost(@RequestBody UUID orderId) {
        return orderService.calculateTotalCost(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/calculate/delivery")
    public OrderDto calculateDeliveryCost(@RequestBody UUID orderId) {
        return orderService.calculateDeliveryCost(orderId);
    }

    @Override
    @PostMapping("/api/v1/order/return")
    public OrderDto productReturn(@RequestBody ProductReturnRequest request) {
        return orderService.productReturn(request);
    }

    @Override
    @GetMapping("/api/v1/order/{orderId}")
    public OrderDto getOrderById(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId);
    }
}

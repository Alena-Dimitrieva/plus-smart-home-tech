package ru.yandex.practicum.api;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.*;
import java.util.List;
import java.util.UUID;

public interface OrderApi {

    @GetMapping("/api/v1/order")
    List<OrderDto> getClientOrders(@RequestParam String username);

    @PutMapping("/api/v1/order")
    OrderDto createNewOrder(@RequestBody CreateNewOrderRequest request,
                            @RequestParam String username);

    @PostMapping("/api/v1/order/payment")
    OrderDto payment(@RequestBody UUID orderId);

    // Дополнительные методы для обратных вызовов от payment/delivery
    @PostMapping("/api/v1/order/payment/success")
    void paymentSuccess(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/payment/failed")
    void paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    void assemblyFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/success")
    void deliverySuccess(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    void deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/return")
    OrderDto productReturn(@RequestBody ProductReturnRequest request);

    @GetMapping("/api/v1/order/{orderId}")
    OrderDto getOrderById(@PathVariable UUID orderId);
}

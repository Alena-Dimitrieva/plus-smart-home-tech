package ru.yandex.practicum.order.service;

import ru.yandex.practicum.dto.*;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderDto createNewOrder(CreateNewOrderRequest request, String username);
    OrderDto payment(UUID orderId);
    void paymentSuccess(UUID orderId);
    void paymentFailed(UUID orderId);
    OrderDto assembly(UUID orderId);
    void assemblyFailed(UUID orderId);
    OrderDto delivery(UUID orderId);
    void deliverySuccess(UUID orderId);
    void deliveryFailed(UUID orderId);
    OrderDto complete(UUID orderId);
    OrderDto calculateTotalCost(UUID orderId);
    OrderDto calculateDeliveryCost(UUID orderId);
    OrderDto productReturn(ProductReturnRequest request);
    List<OrderDto> getClientOrders(String username);
    OrderDto getOrderById(UUID orderId);
}

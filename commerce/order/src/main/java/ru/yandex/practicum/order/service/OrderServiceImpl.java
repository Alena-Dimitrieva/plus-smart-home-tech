package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.*;
import ru.yandex.practicum.model.OrderState;
import ru.yandex.practicum.order.entity.Order;
import ru.yandex.practicum.order.mapper.OrderMapper;
import ru.yandex.practicum.order.repository.OrderRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final WarehouseFeignClient warehouseClient;
    private final DeliveryFeignClient deliveryClient;
    private final PaymentFeignClient paymentClient;
    private final OrderMapper orderMapper;

    @Override
    public OrderDto createNewOrder(CreateNewOrderRequest request, String username) {
        ShoppingCartDto cart = request.getShoppingCart();

        BookedProductsDto booked = warehouseClient.checkProductQuantityEnoughForShoppingCart(cart);
        Order order = orderMapper.toEntity(cart, request.getDeliveryAddress(), username);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());

        OrderDto orderDto = orderMapper.toDto(order);
        double deliveryCost = deliveryClient.deliveryCost(orderDto);
        double productCost = paymentClient.productCost(orderDto);
        order.setDeliveryPrice(deliveryCost);
        order.setProductPrice(productCost);

        return saveOrderWithTotalCost(order);
    }

    @Transactional
    protected OrderDto saveOrderWithTotalCost(Order order) {
        OrderDto orderDto = orderMapper.toDto(order);
        double totalCost = paymentClient.getTotalCost(orderDto);
        order.setTotalPrice(totalCost);
        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.NEW) {
            throw new IllegalStateException("Order state is not NEW");
        }
        PaymentDto payment = paymentClient.payment(orderMapper.toDto(order));
        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.ON_PAYMENT) {
            throw new IllegalStateException("Order state is not ON_PAYMENT");
        }
        order.setState(OrderState.PAID);
    }

    @Override
    @Transactional
    public void paymentFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
    }

    @Override
    @Transactional
    public OrderDto assembly(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.PAID) {
            throw new IllegalStateException("Order state is not PAID");
        }
        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest();
        request.setOrderId(orderId);
        request.setProducts(order.getProducts());
        BookedProductsDto booked = warehouseClient.assemblyProductsForOrder(request);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());
        order.setState(OrderState.ASSEMBLED);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public void assemblyFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
    }

    @Override
    @Transactional
    public OrderDto delivery(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.ASSEMBLED) {
            throw new IllegalStateException("Order state is not ASSEMBLED");
        }
        DeliveryDto deliveryDto = new DeliveryDto();
        deliveryDto.setOrderId(orderId);
        AddressDto from = warehouseClient.getWarehouseAddress();
        deliveryDto.setFromAddress(from);
        AddressDto to = orderMapper.toAddressDto(order.getDeliveryAddress());
        deliveryDto.setToAddress(to);

        DeliveryDto created = deliveryClient.planDelivery(deliveryDto);
        order.setDeliveryId(created.getDeliveryId());
        order.setState(OrderState.ON_DELIVERY);
        deliveryClient.deliveryPicked(orderId);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public void deliverySuccess(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERED);
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.DELIVERED) {
            throw new IllegalStateException("Order state is not DELIVERED");
        }
        order.setState(OrderState.COMPLETED);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrder(orderId);
        OrderDto dto = orderMapper.toDto(order);
        double total = paymentClient.getTotalCost(dto);
        order.setTotalPrice(total);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrder(orderId);
        OrderDto dto = orderMapper.toDto(order);
        double cost = deliveryClient.deliveryCost(dto);
        order.setDeliveryPrice(cost);
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        UUID orderId = request.getOrderId();
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.DELIVERED && order.getState() != OrderState.COMPLETED) {
            throw new IllegalStateException("Return allowed only for DELIVERED or COMPLETED orders");
        }
        warehouseClient.acceptReturn(request.getProducts());
        order.setState(OrderState.PRODUCT_RETURNED);
        return orderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> getClientOrders(String username) {
        return orderRepository.findByUsername(username)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrderById(UUID orderId) {
        Order order = findOrder(orderId);
        return orderMapper.toDto(order);
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
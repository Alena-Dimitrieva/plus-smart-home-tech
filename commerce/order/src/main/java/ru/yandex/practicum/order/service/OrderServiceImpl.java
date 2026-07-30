package ru.yandex.practicum.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.*;
import ru.yandex.practicum.model.OrderState;
import ru.yandex.practicum.order.entity.AddressEmbeddable;
import ru.yandex.practicum.order.entity.Order;
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

    @Override
    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request, String username) {
        ShoppingCartDto cart = request.getShoppingCart();
        BookedProductsDto booked = warehouseClient.checkProductQuantityEnoughForShoppingCart(cart);

        Order order = new Order();
        order.setShoppingCartId(cart.getShoppingCartId());
        order.setProducts(cart.getProducts());
        order.setUsername(username);
        order.setState(OrderState.NEW);
        order.setDeliveryWeight(booked.getDeliveryWeight());
        order.setDeliveryVolume(booked.getDeliveryVolume());
        order.setFragile(booked.isFragile());
        order.setDeliveryAddress(toEmbeddable(request.getDeliveryAddress()));

        OrderDto orderDto = toDto(order);
        double deliveryCost = deliveryClient.deliveryCost(orderDto);
        order.setDeliveryPrice(deliveryCost);

        double productCost = paymentClient.productCost(orderDto);
        order.setProductPrice(productCost);
        orderDto = toDto(order);
        double totalCost = paymentClient.getTotalCost(orderDto);
        order.setTotalPrice(totalCost);

        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.NEW) {
            throw new IllegalStateException("Order state is not NEW");
        }
        PaymentDto payment = paymentClient.payment(toDto(order));
        order.setPaymentId(payment.getPaymentId());
        order.setState(OrderState.ON_PAYMENT);
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Transactional
    public void paymentSuccess(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.ON_PAYMENT) {
            throw new IllegalStateException("Order state is not ON_PAYMENT");
        }
        order.setState(OrderState.PAID);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void paymentFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        orderRepository.save(order);
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
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Transactional
    public void assemblyFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        orderRepository.save(order);
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
        AddressDto to = toAddressDto(order.getDeliveryAddress());
        deliveryDto.setToAddress(to);

        DeliveryDto created = deliveryClient.planDelivery(deliveryDto);
        order.setDeliveryId(created.getDeliveryId());
        order.setState(OrderState.ON_DELIVERY);
        order = orderRepository.save(order);
        // сразу передаём в доставку (эмуляция picked)
        deliveryClient.deliveryPicked(orderId);
        return toDto(order);
    }

    @Override
    @Transactional
    public void deliverySuccess(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deliveryFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getState() != OrderState.DELIVERED) {
            throw new IllegalStateException("Order state is not DELIVERED");
        }
        order.setState(OrderState.COMPLETED);
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrder(orderId);
        OrderDto dto = toDto(order);
        double total = paymentClient.getTotalCost(dto);
        order.setTotalPrice(total);
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrder(orderId);
        OrderDto dto = toDto(order);
        double cost = deliveryClient.deliveryCost(dto);
        order.setDeliveryPrice(cost);
        order = orderRepository.save(order);
        return toDto(order);
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
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    public List<OrderDto> getClientOrders(String username) {
        return orderRepository.findByUsername(username)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderDto getOrderById(UUID orderId) {
        Order order = findOrder(orderId);
        return toDto(order);
    }

    // вспомогательные методы
    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getId());
        dto.setShoppingCartId(order.getShoppingCartId());
        dto.setProducts(order.getProducts());
        dto.setPaymentId(order.getPaymentId());
        dto.setDeliveryId(order.getDeliveryId());
        dto.setState(order.getState());
        dto.setDeliveryWeight(order.getDeliveryWeight());
        dto.setDeliveryVolume(order.getDeliveryVolume());
        dto.setFragile(order.isFragile());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setDeliveryPrice(order.getDeliveryPrice());
        dto.setProductPrice(order.getProductPrice());
        if (order.getDeliveryAddress() != null) {
            dto.setDeliveryAddress(toAddressDto(order.getDeliveryAddress()));
        }
        return dto;
    }

    private AddressEmbeddable toEmbeddable(AddressDto dto) {
        if (dto == null) return null;
        AddressEmbeddable emb = new AddressEmbeddable();
        emb.setCountry(dto.getCountry());
        emb.setCity(dto.getCity());
        emb.setStreet(dto.getStreet());
        emb.setHouse(dto.getHouse());
        emb.setFlat(dto.getFlat());
        return emb;
    }

    private AddressDto toAddressDto(AddressEmbeddable emb) {
        if (emb == null) return null;
        AddressDto dto = new AddressDto();
        dto.setCountry(emb.getCountry());
        dto.setCity(emb.getCity());
        dto.setStreet(emb.getStreet());
        dto.setHouse(emb.getHouse());
        dto.setFlat(emb.getFlat());
        return dto;
    }
}
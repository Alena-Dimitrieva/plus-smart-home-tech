package ru.yandex.practicum.order.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.model.OrderState;
import ru.yandex.practicum.order.entity.AddressEmbeddable;
import ru.yandex.practicum.order.entity.Order;

@Component
public class OrderMapper {

    public Order toEntity(ShoppingCartDto cart, AddressDto deliveryAddress, String username) {
        Order order = new Order();
        order.setShoppingCartId(cart.getShoppingCartId());
        order.setProducts(cart.getProducts());
        order.setUsername(username);
        order.setState(OrderState.NEW);
        order.setDeliveryAddress(toEmbeddable(deliveryAddress));
        return order;
    }

    public OrderDto toDto(Order order) {
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

    // Вспомогательные методы маппинга адресов
    public AddressDto toAddressDto(AddressEmbeddable emb) {
        if (emb == null) return null;
        AddressDto dto = new AddressDto();
        dto.setCountry(emb.getCountry());
        dto.setCity(emb.getCity());
        dto.setStreet(emb.getStreet());
        dto.setHouse(emb.getHouse());
        dto.setFlat(emb.getFlat());
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
}
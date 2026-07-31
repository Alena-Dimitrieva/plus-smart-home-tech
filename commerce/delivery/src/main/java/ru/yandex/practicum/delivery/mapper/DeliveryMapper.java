package ru.yandex.practicum.delivery.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.delivery.entity.AddressEmbeddable;
import ru.yandex.practicum.delivery.entity.Delivery;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.model.DeliveryState;

@Component
public class DeliveryMapper {

    public Delivery toEntity(DeliveryDto dto, OrderDto order) {
        Delivery delivery = new Delivery();
        delivery.setOrderId(dto.getOrderId());
        delivery.setFromAddress(toEmbeddable(dto.getFromAddress()));
        delivery.setToAddress(toEmbeddable(dto.getToAddress()));
        delivery.setWeight(order.getDeliveryWeight());
        delivery.setVolume(order.getDeliveryVolume());
        delivery.setFragile(order.isFragile());
        delivery.setStatus(DeliveryState.CREATED);
        return delivery;
    }

    public DeliveryDto toDto(Delivery delivery) {
        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(delivery.getId());
        dto.setOrderId(delivery.getOrderId());
        dto.setFromAddress(toAddressDto(delivery.getFromAddress()));
        dto.setToAddress(toAddressDto(delivery.getToAddress()));
        dto.setDeliveryState(delivery.getStatus());
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

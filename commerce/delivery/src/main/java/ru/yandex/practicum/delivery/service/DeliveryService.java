package ru.yandex.practicum.delivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.delivery.entity.AddressEmbeddable;
import ru.yandex.practicum.delivery.entity.Delivery;
import ru.yandex.practicum.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.dto.OrderDto;
import ru.yandex.practicum.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.feign.OrderFeignClient;
import ru.yandex.practicum.feign.WarehouseFeignClient;
import ru.yandex.practicum.model.DeliveryState;

import java.util.UUID;

@Service
@Slf4j
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final WarehouseFeignClient warehouseClient;
    private final OrderFeignClient orderClient;
    private final double baseRate;
    private final double address1Coeff;
    private final double address2Coeff;
    private final double fragileMultiplier;
    private final double weightMultiplier;
    private final double volumeMultiplier;
    private final double streetDifferentMultiplier;

    public DeliveryService(DeliveryRepository deliveryRepository,
                           WarehouseFeignClient warehouseClient,
                           OrderFeignClient orderClient,
                           @Value("${delivery.base-rate:5.0}") double baseRate,
                           @Value("${delivery.address-coefficient.ADDRESS_1:1}") double address1Coeff,
                           @Value("${delivery.address-coefficient.ADDRESS_2:2}") double address2Coeff,
                           @Value("${delivery.fragile-multiplier:0.2}") double fragileMultiplier,
                           @Value("${delivery.weight-multiplier:0.3}") double weightMultiplier,
                           @Value("${delivery.volume-multiplier:0.2}") double volumeMultiplier,
                           @Value("${delivery.street-different-multiplier:0.2}") double streetDifferentMultiplier) {
        this.deliveryRepository = deliveryRepository;
        this.warehouseClient = warehouseClient;
        this.orderClient = orderClient;
        this.baseRate = baseRate;
        this.address1Coeff = address1Coeff;
        this.address2Coeff = address2Coeff;
        this.fragileMultiplier = fragileMultiplier;
        this.weightMultiplier = weightMultiplier;
        this.volumeMultiplier = volumeMultiplier;
        this.streetDifferentMultiplier = streetDifferentMultiplier;
    }

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        UUID orderId = deliveryDto.getOrderId();
        OrderDto order = orderClient.getOrderById(orderId);

        Delivery delivery = new Delivery();
        delivery.setOrderId(orderId);
        delivery.setFromAddress(toEmbeddable(deliveryDto.getFromAddress()));
        delivery.setToAddress(toEmbeddable(deliveryDto.getToAddress()));
        delivery.setWeight(order.getDeliveryWeight());
        delivery.setVolume(order.getDeliveryVolume());
        delivery.setFragile(order.isFragile());
        delivery.setStatus(DeliveryState.CREATED);
        delivery = deliveryRepository.save(delivery);

        return toDto(delivery);
    }

    public double deliveryCost(OrderDto order) {
        AddressDto warehouseAddr = warehouseClient.getWarehouseAddress();
        double cost = baseRate;

        String warehouseStreet = warehouseAddr.getStreet();
        if (warehouseStreet != null && warehouseStreet.contains("ADDRESS_1")) {
            cost = cost * address1Coeff + baseRate;
        } else if (warehouseStreet != null && warehouseStreet.contains("ADDRESS_2")) {
            cost = cost * address2Coeff + baseRate;
        }

        if (order.isFragile()) {
            cost += cost * fragileMultiplier;
        }

        cost += order.getDeliveryWeight() * weightMultiplier;
        cost += order.getDeliveryVolume() * volumeMultiplier;

        AddressDto deliveryAddress = order.getDeliveryAddress();
        if (deliveryAddress != null && warehouseStreet != null &&
                !warehouseStreet.equals(deliveryAddress.getStreet())) {
            cost += cost * streetDifferentMultiplier;
        }

        return cost;
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order " + orderId));
        delivery.setStatus(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest();
        request.setOrderId(orderId);
        request.setDeliveryId(delivery.getId());
        warehouseClient.shippedToDelivery(request);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order " + orderId));
        delivery.setStatus(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);
        orderClient.deliverySuccess(orderId);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Delivery not found for order " + orderId));
        delivery.setStatus(DeliveryState.FAILED);
        deliveryRepository.save(delivery);
        orderClient.deliveryFailed(orderId);
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

    private DeliveryDto toDto(Delivery delivery) {
        DeliveryDto dto = new DeliveryDto();
        dto.setDeliveryId(delivery.getId());
        dto.setOrderId(delivery.getOrderId());
        dto.setFromAddress(toAddressDto(delivery.getFromAddress()));
        dto.setToAddress(toAddressDto(delivery.getToAddress()));
        dto.setDeliveryState(delivery.getStatus());
        return dto;
    }
}
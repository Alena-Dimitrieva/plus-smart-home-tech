package ru.yandex.practicum.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.warehouse.entity.OrderBooking;
import java.util.List;
import java.util.UUID;

public interface OrderBookingRepository extends JpaRepository<OrderBooking, UUID> {
    List<OrderBooking> findByOrderId(UUID orderId);
    void deleteByOrderId(UUID orderId);
}
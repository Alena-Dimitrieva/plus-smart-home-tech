package ru.yandex.practicum.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}

package ru.yandex.practicum.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.cart.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUsername(String username);
}

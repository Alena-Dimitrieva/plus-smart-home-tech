package ru.yandex.practicum.cart.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carts")
@Data
public class Cart {
    @Id
    @Column(name = "cart_id")
    private UUID id = UUID.randomUUID();

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "active")
    private boolean active = true;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}
package ru.yandex.practicum.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.AvailabilityResponse;
import ru.yandex.practicum.dto.CartDto;
import ru.yandex.practicum.dto.CartItemDto;
import ru.yandex.practicum.feign.WarehouseFeignClient;
import ru.yandex.practicum.cart.entity.Cart;
import ru.yandex.practicum.cart.entity.CartItem;
import ru.yandex.practicum.cart.exception.NotFoundException;
import ru.yandex.practicum.cart.repository.CartRepository;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final WarehouseFeignClient warehouseClient;

    public CartDto getCart(String username) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + username));
        return toDto(cart);
    }

    @Transactional
    public CartDto addItem(String username, UUID productId, int quantity) {
        Cart cart = getOrCreateCart(username);
        if (!cart.isActive()) {
            throw new IllegalStateException("Cart is deactivated");
        }

        CartDto cartDto = toDto(cart);
        AvailabilityResponse response = warehouseClient.checkAvailability(cartDto);
        if (!response.isAvailable() && response.getMissingProductIds().contains(productId)) {
            throw new IllegalArgumentException("Product " + productId + " is not available in required quantity");
        }

        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst().orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getItems().add(newItem);
        }
        Cart saved = cartRepository.save(cart);
        return toDto(saved);
    }

    @Transactional
    public CartDto removeItem(String username, UUID productId) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + username));
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return toDto(cartRepository.save(cart));
    }

    @Transactional
    public boolean deactivateCart(String username) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + username));
        cart.setActive(false);
        cartRepository.save(cart);
        return true;
    }

    private Cart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUsername(username);
                    newCart.setActive(true);
                    return cartRepository.save(newCart);
                });
    }

    private CartDto toDto(Cart cart) {
        CartDto dto = new CartDto();
        dto.setUsername(cart.getUsername());
        dto.setActive(cart.isActive());
        dto.setItems(cart.getItems().stream().map(item -> {
            CartItemDto itemDto = new CartItemDto();
            itemDto.setProductId(item.getProductId());
            itemDto.setQuantity(item.getQuantity());
            return itemDto;
        }).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public CartDto changeQuantity(String username, UUID productId, int quantity) {
        Cart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + username));
        if (!cart.isActive()) {
            throw new IllegalStateException("Cart is deactivated");
        }
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product not in cart"));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return toDto(cartRepository.save(cart));
    }
}

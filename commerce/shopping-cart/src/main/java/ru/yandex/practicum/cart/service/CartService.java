package ru.yandex.practicum.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.feign.WarehouseFeignClient;
import ru.yandex.practicum.cart.entity.Cart;
import ru.yandex.practicum.cart.entity.CartItem;
import ru.yandex.practicum.cart.repository.CartRepository;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final WarehouseFeignClient warehouseClient;

    public ShoppingCartDto getCart(String username) {
        Cart cart = cartRepository.findByUsername(username).orElseGet(() -> createCart(username));
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        Cart cart = getOrCreateCart(username);
        if (!cart.isActive()) throw new NotAuthorizedUserException();
        ShoppingCartDto dto = toDto(cart);
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            dto.getProducts().merge(entry.getKey(), entry.getValue(), Long::sum);
        }
        warehouseClient.checkProductQuantityEnoughForShoppingCart(dto);
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            addOrUpdateItem(cart, entry.getKey(), entry.getValue());
        }
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        Cart cart = getOrCreateCart(username);
        if (!cart.isActive()) throw new NotAuthorizedUserException();
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst().orElseThrow(NoProductsInShoppingCartException::new);
        if (request.getNewQuantity() == 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity((int) request.getNewQuantity());
        }
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        Cart cart = getOrCreateCart(username);
        if (!cart.isActive()) throw new NotAuthorizedUserException();
        cart.getItems().removeIf(item -> productIds.contains(item.getProductId()));
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public void deactivateCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.setActive(false);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username).orElseGet(() -> createCart(username));
    }

    private Cart createCart(String username) {
        Cart c = new Cart();
        c.setUsername(username);
        c.setActive(true);
        return cartRepository.save(c);
    }

    private void addOrUpdateItem(Cart cart, UUID productId, long quantity) {
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId)).findFirst().orElse(null);
        if (existing != null) {
            existing.setQuantity((int) (existing.getQuantity() + quantity));
        } else {
            CartItem item = new CartItem();
            item.setProductId(productId);
            item.setQuantity((int) quantity);
            item.setCart(cart);
            cart.getItems().add(item);
        }
    }

    private ShoppingCartDto toDto(Cart cart) {
        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setShoppingCartId(UUID.randomUUID());
        Map<UUID, Long> products = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            products.put(item.getProductId(), (long) item.getQuantity());
        }
        dto.setProducts(products);
        return dto;
    }
}
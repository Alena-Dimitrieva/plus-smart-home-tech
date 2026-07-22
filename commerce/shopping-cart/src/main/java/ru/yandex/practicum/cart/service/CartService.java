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

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new NotAuthorizedUserException("Username must not be empty");
        }
    }

    private Cart getActiveCartOrCreate(String username) {
        return cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> createCart(username));
    }

    private Cart createCart(String username) {
        Cart c = new Cart();
        c.setUsername(username);
        c.setActive(true);
        return cartRepository.save(c);
    }

    public ShoppingCartDto getCart(String username) {
        validateUsername(username);
        Cart cart = getActiveCartOrCreate(username);
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        validateUsername(username);
        if (products == null) {
            products = new HashMap<>();
        }
        Cart cart = getActiveCartOrCreate(username);
        if (!cart.isActive()) {
            throw new NotAuthorizedUserException("Cart is deactivated");
        }

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
        validateUsername(username);
        if (request == null) {
            throw new IllegalArgumentException("Request body must not be null");
        }
        Cart cart = getActiveCartOrCreate(username);
        if (!cart.isActive()) {
            throw new NotAuthorizedUserException("Cart is deactivated");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new NoProductsInShoppingCartException("Product not found in cart"));

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
        validateUsername(username);
        List<UUID> ids = productIds != null ? productIds : Collections.emptyList();
        Cart cart = getActiveCartOrCreate(username);
        if (!cart.isActive()) {
            throw new NotAuthorizedUserException("Cart is deactivated");
        }

        int beforeSize = cart.getItems().size();
        cart.getItems().removeIf(item -> ids.contains(item.getProductId()));
        if (beforeSize == cart.getItems().size()) {
            throw new NoProductsInShoppingCartException("None of the specified products were found in the cart");
        }
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public void deactivateCart(String username) {
        validateUsername(username);
        Cart cart = cartRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new NotAuthorizedUserException("No active cart found for user"));
        cart.setActive(false);
        cartRepository.save(cart);
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
        dto.setShoppingCartId(cart.getId());
        Map<UUID, Long> products = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            products.put(item.getProductId(), (long) item.getQuantity());
        }
        dto.setProducts(products);
        return dto;
    }
}
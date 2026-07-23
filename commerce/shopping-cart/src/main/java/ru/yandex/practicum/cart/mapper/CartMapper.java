package ru.yandex.practicum.cart.mapper;

import ru.yandex.practicum.dto.ShoppingCartDto;
import ru.yandex.practicum.cart.entity.Cart;
import ru.yandex.practicum.cart.entity.CartItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CartMapper {

    public static ShoppingCartDto toDto(Cart cart) {
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

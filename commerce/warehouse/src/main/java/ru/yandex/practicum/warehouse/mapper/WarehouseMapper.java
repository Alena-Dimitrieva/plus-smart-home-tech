package ru.yandex.practicum.warehouse.mapper;

import ru.yandex.practicum.dto.NewProductInWarehouseRequest;
import ru.yandex.practicum.warehouse.entity.WarehouseProduct;

public class WarehouseMapper {

    public static WarehouseProduct toEntity(NewProductInWarehouseRequest request) {
        WarehouseProduct wp = new WarehouseProduct();
        wp.setProductId(request.getProductId());
        wp.setQuantity(0);
        wp.setWidth(request.getDimension().getWidth());
        wp.setHeight(request.getDimension().getHeight());
        wp.setDepth(request.getDimension().getDepth());
        wp.setWeight(request.getWeight());
        wp.setFragile(request.getFragile() != null && request.getFragile());
        return wp;
    }
}

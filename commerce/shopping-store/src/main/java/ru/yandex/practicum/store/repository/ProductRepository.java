package ru.yandex.practicum.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.ProductCategory;
import ru.yandex.practicum.model.ProductState;
import ru.yandex.practicum.store.entity.Product;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByProductCategoryAndProductState(
            ProductCategory productCategory,
            ProductState productState,
            Pageable pageable
    );

}
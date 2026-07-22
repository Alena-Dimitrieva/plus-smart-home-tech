package ru.yandex.practicum.store.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class SortUtils {

    public Sort parseSort(String[] sortArray) {
        if (sortArray == null || sortArray.length == 0) {
            return Sort.unsorted();
        }

        log.debug("Parsing sort array: {}", Arrays.toString(sortArray));

        List<Sort.Order> orders = new ArrayList<>();

        for (int i = 0; i < sortArray.length; i++) {
            String trimmed = sortArray[i].trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (trimmed.contains(",")) {
                String[] parts = trimmed.split(",");
                String property = parts[0].trim();
                if (property.isEmpty()) {
                    continue;
                }
                Sort.Direction direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
                orders.add(new Sort.Order(direction, property));
                continue;
            }

            if (("asc".equalsIgnoreCase(trimmed) || "desc".equalsIgnoreCase(trimmed)) && !orders.isEmpty()) {
                Sort.Order lastOrder = orders.remove(orders.size() - 1);
                Sort.Direction dir = "desc".equalsIgnoreCase(trimmed) ? Sort.Direction.DESC : Sort.Direction.ASC;
                orders.add(new Sort.Order(dir, lastOrder.getProperty()));
                continue;
            }

            orders.add(new Sort.Order(Sort.Direction.ASC, trimmed));
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}
package ru.yandex.practicum.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.model.QuantityState;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetProductQuantityStateRequest {
    @NotNull(message = "productId не должен быть null")
    private UUID productId;

    @NotNull(message = "quantityState не должен быть null")
    private QuantityState quantityState;
}
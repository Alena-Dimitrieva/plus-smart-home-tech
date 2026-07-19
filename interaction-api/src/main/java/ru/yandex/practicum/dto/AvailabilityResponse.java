package ru.yandex.practicum.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class AvailabilityResponse {
    private boolean available;
    private List<UUID> missingProductIds;
}

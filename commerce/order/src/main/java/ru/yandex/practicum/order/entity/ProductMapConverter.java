package ru.yandex.practicum.order.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

@Converter
@RequiredArgsConstructor
@Slf4j
public class ProductMapConverter implements AttributeConverter<Map<UUID, Long>, String> {
    private final ObjectMapper mapper;

    @Override
    public String convertToDatabaseColumn(Map<UUID, Long> attribute) {
        try {
            return mapper.writeValueAsString(attribute);
        } catch (Exception e) {
            log.error("Error converting map to JSON", e);
            throw new RuntimeException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<UUID, Long> convertToEntityAttribute(String dbData) {
        try {
            return mapper.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error converting JSON to map", e);
            throw new RuntimeException("Error converting JSON to map", e);
        }
    }
}
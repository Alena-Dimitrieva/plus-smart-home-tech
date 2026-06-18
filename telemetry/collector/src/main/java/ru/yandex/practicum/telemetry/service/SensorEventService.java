package ru.yandex.practicum.telemetry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.mapper.SensorEventMapper;
import ru.yandex.practicum.telemetry.model.sensor.SensorEvent;


@Service
@RequiredArgsConstructor
@Slf4j
public class SensorEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SensorEventMapper mapper;

    @Value("${kafka.topic.sensors}")
    private String sensorsTopic;

    public void processSensorEvent(SensorEvent event) {
        var avroEvent = mapper.toAvro(event);
        log.debug("Sending to Kafka: topic={}, key={}, event={}", sensorsTopic, event.getId(), avroEvent);
        kafkaTemplate.send(sensorsTopic, event.getId(), avroEvent);
    }
}

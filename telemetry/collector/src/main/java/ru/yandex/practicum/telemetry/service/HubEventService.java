package ru.yandex.practicum.telemetry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.telemetry.mapper.HubEventMapper;
import ru.yandex.practicum.telemetry.model.hub.HubEvent;


@Service
@RequiredArgsConstructor
@Slf4j
public class HubEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final HubEventMapper mapper;

    @Value("${kafka.topic.hubs}")
    private String hubsTopic;

    public void processHubEvent(HubEvent event) {
        var avroEvent = mapper.toAvro(event);
        log.debug("Sending to Kafka: topic={}, key={}, event={}", hubsTopic, event.getHubId(), avroEvent);
        kafkaTemplate.send(hubsTopic, event.getHubId(), avroEvent);
    }
}
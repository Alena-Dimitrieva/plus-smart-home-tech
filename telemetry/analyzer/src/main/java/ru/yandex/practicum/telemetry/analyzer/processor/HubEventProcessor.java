package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.service.HubEventService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final ConsumerFactory<String, HubEventAvro> consumerFactory;
    private final HubEventService hubEventService;

    @Value("${kafka.topic.hubs}")
    private String hubsTopic;

    private volatile boolean running = true;

    @Override
    public void run() {
        try (Consumer<String, HubEventAvro> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of(hubsTopic));
            log.info("HubEventProcessor подписан на топик: {}", hubsTopic);

            while (running) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    HubEventAvro event = record.value();
                    log.info("Получено событие хаба: hubId={}, payload={}",
                            event.getHubId(), event.getPayload().getClass().getSimpleName());

                    hubEventService.processHubEvent(event);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.info("HubEventProcessor остановлен");
        } catch (Exception e) {
            log.error("Ошибка в HubEventProcessor", e);
        }
    }

    public void stop() {
        running = false;
    }
}


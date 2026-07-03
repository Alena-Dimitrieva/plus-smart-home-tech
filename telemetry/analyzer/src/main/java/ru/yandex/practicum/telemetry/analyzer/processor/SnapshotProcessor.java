package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.service.ScenarioService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final ConsumerFactory<String, SensorsSnapshotAvro> consumerFactory;
    private final ScenarioService scenarioService;

    @Value("${kafka.topic.snapshots}")
    private String snapshotsTopic;

    private volatile boolean running = true;

    public void start() {
        try (Consumer<String, SensorsSnapshotAvro> consumer = consumerFactory.createConsumer()) {
            consumer.subscribe(List.of(snapshotsTopic));
            log.info("SnapshotProcessor подписан на топик: {}", snapshotsTopic);

            while (running) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    SensorsSnapshotAvro snapshot = record.value();
                    log.info("Получен снапшот: hubId={}, timestamp={}, датчиков={}",
                            snapshot.getHubId(), snapshot.getTimestamp(), snapshot.getSensorsState().size());

                    scenarioService.processSnapshot(snapshot);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.info("SnapshotProcessor остановлен");
        } catch (Exception e) {
            log.error("Ошибка в SnapshotProcessor", e);
        }
    }

    public void stop() {
        running = false;
    }
}
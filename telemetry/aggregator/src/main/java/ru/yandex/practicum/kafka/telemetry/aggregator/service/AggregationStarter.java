package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    @Value("${kafka.topic.sensors}")
    private String sensorsTopic;

    @Value("${kafka.topic.hubs}")
    private String hubsTopic;

    @Value("${kafka.topic.snapshots}")
    private String snapshotsTopic;

    private final ConsumerFactory<String, SensorEventAvro> sensorConsumerFactory;
    private final ConsumerFactory<String, HubEventAvro> hubConsumerFactory;
    private final KafkaTemplate<String, SensorsSnapshotAvro> kafkaTemplate;
    private final SnapshotService snapshotService;

    private volatile boolean running = true;

    public void start() {
        Thread sensorsThread = new Thread(this::pollSensors);
        sensorsThread.setName("sensors-consumer");
        sensorsThread.start();

        Thread hubsThread = new Thread(this::pollHubs);
        hubsThread.setName("hubs-consumer");
        hubsThread.start();

        try {
            sensorsThread.join();
            hubsThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void pollSensors() {
        try (Consumer<String, SensorEventAvro> consumer = sensorConsumerFactory.createConsumer()) {
            consumer.subscribe(List.of(sensorsTopic));
            log.info("Подписан на топик сенсоров: {}", sensorsTopic);

            while (running) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    SensorEventAvro event = record.value();
                    log.debug("Обработка сенсорного события: {}", event);
                    snapshotService.updateState(event)
                            .ifPresent(snapshot -> {
                                kafkaTemplate.send(snapshotsTopic, snapshot.getHubId(), snapshot);
                                log.debug("Снапшот отправлен в топик: {}", snapshotsTopic);
                            });
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки сенсоров", e);
        } finally {
            log.info("Поток сенсоров завершён");
        }
    }

    private void pollHubs() {
        try (Consumer<String, HubEventAvro> consumer = hubConsumerFactory.createConsumer()) {
            consumer.subscribe(List.of(hubsTopic));
            log.info("Подписан на топик хабов: {}", hubsTopic);

            while (running) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(500));
                for (var record : records) {
                    HubEventAvro event = record.value();
                    log.debug("Получено событие хаба (пропускаем): {}", event);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            // нормальное завершение
        } catch (Exception e) {
            log.error("Ошибка в цикле обработки хабов", e);
        } finally {
            log.info("Поток хабов завершён");
        }
    }

    public void stop() {
        running = false;
    }
}
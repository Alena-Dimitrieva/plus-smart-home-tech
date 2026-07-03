package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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

        log.info("Aggregator started");

        Thread sensorsThread = new Thread(this::pollSensors);
        sensorsThread.setName("sensors-consumer");

        Thread hubsThread = new Thread(this::pollHubs);
        hubsThread.setName("hubs-consumer");

        sensorsThread.start();
        hubsThread.start();

        try {
            sensorsThread.join();
            hubsThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void pollSensors() {

        log.info("Sensors thread started");

        try (Consumer<String, SensorEventAvro> consumer =
                     sensorConsumerFactory.createConsumer()) {

            if (sensorsTopic == null) {
                throw new IllegalStateException("sensorsTopic is NULL (check application.properties)");
            }

            consumer.subscribe(List.of(sensorsTopic));
            log.info("Subscribed to {}", sensorsTopic);

            while (running) {

                try {
                    ConsumerRecords<String, SensorEventAvro> records =
                            consumer.poll(Duration.ofMillis(500));

                    for (var record : records) {

                        SensorEventAvro event = record.value();

                        snapshotService.updateState(event)
                                .ifPresent(snapshot -> {
                                    kafkaTemplate.send(
                                            snapshotsTopic,
                                            snapshot.getHubId(),
                                            snapshot
                                    );
                                });
                    }

                    consumer.commitSync();

                } catch (Exception e) {
                    log.error("Error while processing sensor record", e);
                }
            }

        } catch (Exception e) {
            log.error("Fatal error in sensors consumer", e);
        }

        log.info("Sensors thread finished");
    }

    private void pollHubs() {

        log.info("Hubs thread started");

        try (Consumer<String, HubEventAvro> consumer =
                     hubConsumerFactory.createConsumer()) {

            if (hubsTopic == null) {
                throw new IllegalStateException("hubsTopic is NULL");
            }

            consumer.subscribe(List.of(hubsTopic));
            log.info("Subscribed to {}", hubsTopic);

            while (running) {

                try {
                    ConsumerRecords<String, HubEventAvro> records =
                            consumer.poll(Duration.ofMillis(500));

                    for (var record : records) {
                        log.debug("Hub event: {}", record.value());
                    }

                    consumer.commitSync();

                } catch (Exception e) {
                    log.error("Error in hubs loop", e);
                }
            }

        } catch (Exception e) {
            log.error("Fatal error in hubs consumer", e);
        }

        log.info("Hubs thread finished");
    }

    public void stop() {
        running = false;
    }
}
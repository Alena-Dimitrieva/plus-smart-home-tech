package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private static final String INPUT_TOPIC = "telemetry.sensors.v1";
    private static final String OUTPUT_TOPIC = "telemetry.snapshots.v1";

    private final ConsumerFactory<String, SensorEventAvro> consumerFactory;
    private final KafkaTemplate<String, SensorsSnapshotAvro> kafkaTemplate;
    private final SnapshotService snapshotService;

    private Consumer<String, SensorEventAvro> consumer;

    public void start() {

        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singletonList(INPUT_TOPIC));

        try {

            while (true) {

                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {

                    SensorEventAvro event = record.value();

                    snapshotService.updateState(event)
                            .ifPresent(snapshot ->
                                    kafkaTemplate.send(OUTPUT_TOPIC,
                                            snapshot.getHubId(),
                                            snapshot)
                            );
                }

                consumer.commitSync();
            }

        } catch (WakeupException ignored) {
            log.info("Consumer shutdown");
        } catch (Exception e) {
            log.error("Aggregator error", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}

package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {

        String hubId = event.getHubId();
        String sensorId = event.getId();
        Instant eventTime = Instant.ofEpochMilli(event.getTimestamp());

        SensorsSnapshotAvro current = snapshots.get(hubId);

        Map<String, SensorStateAvro> states =
                current == null
                        ? new HashMap<>()
                        : new HashMap<>(current.getSensorsState());

        SensorStateAvro old = states.get(sensorId);

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(eventTime)
                .setData(event.getPayload())
                .build();

        if (old != null && old.equals(newState)) {
            return Optional.empty();
        }

        states.put(sensorId, newState);

        SensorsSnapshotAvro updated = SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(eventTime)
                .setSensorsState(states)
                .build();

        snapshots.put(hubId, updated);

        return Optional.of(updated);
    }
}
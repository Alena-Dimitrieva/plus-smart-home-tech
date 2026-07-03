package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        long eventTimestamp = event.getTimestamp();

        SensorsSnapshotAvro snapshot = snapshots.get(hubId);
        if (snapshot == null) {
            snapshot = SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setTimestamp(Instant.ofEpochMilli(eventTimestamp))
                    .setSensorsState(new HashMap<>())
                    .build();
        }

        Map<String, SensorStateAvro> states = new HashMap<>(snapshot.getSensorsState());

        SensorStateAvro oldState = states.get(sensorId);

        if (oldState != null && oldState.getTimestamp().toEpochMilli() >= eventTimestamp) {
            return Optional.empty();
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(Instant.ofEpochMilli(eventTimestamp))
                .setData(event.getPayload())
                .build();

        states.put(sensorId, newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                .setHubId(hubId)
                .setSensorsState(states)
                .setTimestamp(Instant.ofEpochMilli(eventTimestamp))
                .build();

        snapshots.put(hubId, updatedSnapshot);

        return Optional.of(updatedSnapshot);
    }
}
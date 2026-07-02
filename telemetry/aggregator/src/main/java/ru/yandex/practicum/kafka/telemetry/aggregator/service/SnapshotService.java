package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.*;

@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {

        String hubId = event.getHubId();
        String sensorId = event.getId();
        long eventTimestamp = event.getTimestamp();

        SensorsSnapshotAvro snapshot =
                snapshots.getOrDefault(hubId, createEmptySnapshot(hubId, eventTimestamp));

        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        SensorStateAvro oldState = states.get(sensorId);

        if (oldState != null) {

            long oldTimestamp = oldState.getTimestamp().toEpochMilli();

            if (oldTimestamp >= eventTimestamp) {
                return Optional.empty();
            }

            if (oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setTimestamp(java.time.Instant.ofEpochMilli(eventTimestamp))
                .setData(event.getPayload())
                .build();

        states.put(sensorId, newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                .setHubId(hubId)
                .setSensorsState(states)
                .setTimestamp(java.time.Instant.ofEpochMilli(eventTimestamp))
                .build();

        snapshots.put(hubId, updatedSnapshot);

        return Optional.of(updatedSnapshot);
    }

    private SensorsSnapshotAvro createEmptySnapshot(String hubId, long timestamp) {

        return SensorsSnapshotAvro.newBuilder()
                .setHubId(hubId)
                .setTimestamp(java.time.Instant.ofEpochMilli(timestamp))
                .setSensorsState(new HashMap<>())
                .build();
    }
}

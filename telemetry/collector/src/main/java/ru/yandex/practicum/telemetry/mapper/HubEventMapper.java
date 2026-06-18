package ru.yandex.practicum.telemetry.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.model.hub.*;

import java.util.stream.Collectors;

@Component
public class HubEventMapper {

    public HubEventAvro toAvro(HubEvent event) {
        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp().toEpochMilli());

        switch (event.getType()) {
            case DEVICE_ADDED:
                DeviceAddedEvent added = (DeviceAddedEvent) event;
                builder.setPayload(DeviceAddedEventAvro.newBuilder()
                        .setId(added.getId())
                        .setType(DeviceTypeAvro.valueOf(added.getDeviceType().name()))
                        .build());
                break;
            case DEVICE_REMOVED:
                DeviceRemovedEvent removed = (DeviceRemovedEvent) event;
                builder.setPayload(DeviceRemovedEventAvro.newBuilder()
                        .setId(removed.getId())
                        .build());
                break;
            case SCENARIO_ADDED:
                ScenarioAddedEvent scenarioAdded = (ScenarioAddedEvent) event;
                builder.setPayload(ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioAdded.getName())
                        .setConditions(scenarioAdded.getConditions().stream()
                                .map(this::mapCondition)
                                .collect(Collectors.toList()))
                        .setActions(scenarioAdded.getActions().stream()
                                .map(this::mapAction)
                                .collect(Collectors.toList()))
                        .build());
                break;
            case SCENARIO_REMOVED:
                ScenarioRemovedEvent scenarioRemoved = (ScenarioRemovedEvent) event;
                builder.setPayload(ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemoved.getName())
                        .build());
                break;
            default:
                throw new IllegalArgumentException("Unsupported hub event type: " + event.getType());
        }
        return builder.build();
    }

    private ScenarioConditionAvro mapCondition(ScenarioCondition condition) {
        ScenarioConditionAvro.Builder builder = ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()));
        builder.setValue(condition.getValue());
        return builder.build();
    }

    private DeviceActionAvro mapAction(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .setValue(action.getValue())
                .build();
    }
}
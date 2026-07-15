package ru.yandex.practicum.telemetry.collector.controller.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.model.hub.*;
import ru.yandex.practicum.telemetry.collector.model.sensor.*;
import ru.yandex.practicum.telemetry.collector.service.HubEventService;
import ru.yandex.practicum.telemetry.collector.service.SensorEventService;
import ru.yandex.practicum.telemetry.proto.CollectorControllerGrpc;


import java.time.Instant;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class EventControllerGrpc extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final SensorEventService sensorEventService;
    private final HubEventService hubEventService;

    @Override
    public void collectSensorEvent(SensorEventProto request,
                                   StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получено событие сенсора: {}", request);
            SensorEvent event = mapToSensorEvent(request);
            sensorEventService.processSensorEvent(event);
            log.info("Событие сенсора отправлено в Kafka");

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки сенсорного события", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private SensorEvent mapToSensorEvent(SensorEventProto proto) {
        String id = proto.getId();
        String hubId = proto.getHubId(); // поле hubId (с заглавной)
        Instant timestamp = Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos());

        switch (proto.getPayloadCase()) {
            case TEMPERATURE_SENSOR:
                TemperatureSensorEvent temp = new TemperatureSensorEvent();
                temp.setId(id);
                temp.setHubId(hubId);
                temp.setTimestamp(timestamp);
                temp.setTemperatureC(proto.getTemperatureSensor().getTemperatureC());
                temp.setTemperatureF(proto.getTemperatureSensor().getTemperatureF());
                return temp;

            case LIGHT_SENSOR:
                LightSensorEvent light = new LightSensorEvent();
                light.setId(id);
                light.setHubId(hubId);
                light.setTimestamp(timestamp);
                light.setLuminosity(proto.getLightSensor().getLuminosity());
                light.setLinkQuality(proto.getLightSensor().getLinkQuality());
                return light;

            case MOTION_SENSOR:
                MotionSensorEvent motion = new MotionSensorEvent();
                motion.setId(id);
                motion.setHubId(hubId);
                motion.setTimestamp(timestamp);
                motion.setMotion(proto.getMotionSensor().getMotion());
                motion.setLinkQuality(proto.getMotionSensor().getLinkQuality());
                motion.setVoltage(proto.getMotionSensor().getVoltage());
                return motion;

            case CLIMATE_SENSOR:
                ClimateSensorEvent climate = new ClimateSensorEvent();
                climate.setId(id);
                climate.setHubId(hubId);
                climate.setTimestamp(timestamp);
                climate.setTemperatureC(proto.getClimateSensor().getTemperatureC());
                climate.setHumidity(proto.getClimateSensor().getHumidity());
                climate.setCo2Level(proto.getClimateSensor().getCo2Level());
                return climate;

            case SWITCH_SENSOR:
                SwitchSensorEvent sw = new SwitchSensorEvent();
                sw.setId(id);
                sw.setHubId(hubId);
                sw.setTimestamp(timestamp);
                sw.setState(proto.getSwitchSensor().getState());
                return sw;

            default:
                throw new IllegalArgumentException("Неизвестный тип сенсора: " + proto.getPayloadCase());
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request,
                                StreamObserver<Empty> responseObserver) {
        try {
            log.info("Получено событие хаба: {}", request);
            HubEvent event = mapToHubEvent(request);
            hubEventService.processHubEvent(event);
            log.info("Событие хаба отправлено в Kafka");

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка обработки события хаба", e);
            responseObserver.onError(io.grpc.Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    private HubEvent mapToHubEvent(HubEventProto proto) {
        String hubId = proto.getHubId();
        Instant timestamp = Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos());

        switch (proto.getPayloadCase()) {
            case DEVICE_ADDED:
                DeviceAddedEvent added = new DeviceAddedEvent();
                added.setHubId(hubId);
                added.setTimestamp(timestamp);
                added.setId(proto.getDeviceAdded().getId());
                added.setDeviceType(DeviceType.valueOf(proto.getDeviceAdded().getType().name()));
                return added;

            case DEVICE_REMOVED:
                DeviceRemovedEvent removed = new DeviceRemovedEvent();
                removed.setHubId(hubId);
                removed.setTimestamp(timestamp);
                removed.setId(proto.getDeviceRemoved().getId());
                return removed;

            case SCENARIO_ADDED:
                ScenarioAddedEvent scenarioAdded = new ScenarioAddedEvent();
                scenarioAdded.setHubId(hubId);
                scenarioAdded.setTimestamp(timestamp);
                scenarioAdded.setName(proto.getScenarioAdded().getName());
                scenarioAdded.setConditions(proto.getScenarioAdded().getConditionList().stream()
                        .map(c -> {
                            ScenarioCondition condition = new ScenarioCondition();
                            condition.setSensorId(c.getSensorId());
                            condition.setType(ConditionType.valueOf(c.getType().name()));
                            condition.setOperation(ConditionOperation.valueOf(c.getOperation().name()));
                            // Извлекаем значение из oneof
                            switch (c.getValueCase()) {
                                case BOOL_VALUE:
                                    condition.setValue(c.getBoolValue() ? 1 : 0);
                                    break;
                                case INT_VALUE:
                                    condition.setValue(c.getIntValue());
                                    break;
                                default:
                                    throw new IllegalArgumentException("Неизвестный тип значения условия");
                            }
                            return condition;
                        })
                        .collect(Collectors.toList()));
                scenarioAdded.setActions(proto.getScenarioAdded().getActionList().stream()
                        .map(a -> {
                            DeviceAction action = new DeviceAction();
                            action.setSensorId(a.getSensorId());
                            action.setType(ActionType.valueOf(a.getType().name()));
                            // Проверяем, есть ли значение
                            if (a.hasValue()) {
                                action.setValue(a.getValue());
                            } else {
                                action.setValue(0); // или другое значение по умолчанию
                            }
                            return action;
                        })
                        .collect(Collectors.toList()));
                return scenarioAdded;

            case SCENARIO_REMOVED:
                ScenarioRemovedEvent scenarioRemoved = new ScenarioRemovedEvent();
                scenarioRemoved.setHubId(hubId);
                scenarioRemoved.setTimestamp(timestamp);
                scenarioRemoved.setName(proto.getScenarioRemoved().getName());
                return scenarioRemoved;

            default:
                throw new IllegalArgumentException("Неизвестный тип события хаба: " + proto.getPayloadCase());
        }
    }
}
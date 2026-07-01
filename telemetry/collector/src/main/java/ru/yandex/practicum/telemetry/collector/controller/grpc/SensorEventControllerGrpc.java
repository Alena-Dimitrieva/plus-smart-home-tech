package ru.yandex.practicum.telemetry.collector.controller.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.telemetry.collector.model.sensor.*;
import ru.yandex.practicum.telemetry.collector.service.SensorEventService;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class SensorEventControllerGrpc extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final SensorEventService sensorEventService;

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
        String hubId = proto.getHubId();
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
                motion.setMotion(proto.getMotionSensor().getMotionDetected());
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
                sw.setState(proto.getSwitchSensor().getSwitchState());
                return sw;

            default:
                throw new IllegalArgumentException("Неизвестный тип сенсора: " + proto.getPayloadCase());
        }
    }
}
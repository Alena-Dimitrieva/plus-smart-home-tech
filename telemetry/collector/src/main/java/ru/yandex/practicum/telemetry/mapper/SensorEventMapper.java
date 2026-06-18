package ru.yandex.practicum.telemetry.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.model.sensor.*;

@Component
public class SensorEventMapper {

    public SensorEventAvro toAvro(SensorEvent event) {
        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp().toEpochMilli());

        switch (event.getType()) {
            case LIGHT_SENSOR_EVENT:
                LightSensorEvent light = (LightSensorEvent) event;
                builder.setPayload(LightSensorAvro.newBuilder()
                        .setLinkQuality(light.getLinkQuality())
                        .setLuminosity(light.getLuminosity())
                        .build());
                break;
            case MOTION_SENSOR_EVENT:
                MotionSensorEvent motion = (MotionSensorEvent) event;
                builder.setPayload(MotionSensorAvro.newBuilder()
                        .setLinkQuality(motion.getLinkQuality())
                        .setMotion(motion.isMotion())
                        .setVoltage(motion.getVoltage())
                        .build());
                break;
            case CLIMATE_SENSOR_EVENT:
                ClimateSensorEvent climate = (ClimateSensorEvent) event;
                builder.setPayload(ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climate.getTemperatureC())
                        .setHumidity(climate.getHumidity())
                        .setCo2Level(climate.getCo2Level())
                        .build());
                break;
            case SWITCH_SENSOR_EVENT:
                SwitchSensorEvent sw = (SwitchSensorEvent) event;
                builder.setPayload(SwitchSensorAvro.newBuilder()
                        .setState(sw.isState())
                        .build());
                break;
            case TEMPERATURE_SENSOR_EVENT:
                TemperatureSensorEvent temp = (TemperatureSensorEvent) event;
                builder.setPayload(TemperatureSensorAvro.newBuilder()
                        .setId(temp.getId())
                        .setHubId(temp.getHubId())
                        .setTimestamp(temp.getTimestamp().toEpochMilli())
                        .setTemperatureC(temp.getTemperatureC())
                        .setTemperatureF(temp.getTemperatureF())
                        .build());
                break;
            default:
                throw new IllegalArgumentException("Unsupported sensor event type: " + event.getType());
        }
        return builder.build();
    }
}
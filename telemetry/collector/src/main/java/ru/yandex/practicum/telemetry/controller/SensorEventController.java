package ru.yandex.practicum.telemetry.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.telemetry.model.sensor.SensorEvent;
import ru.yandex.practicum.telemetry.service.SensorEventService;


@RestController
@RequestMapping("/events/sensors")
@RequiredArgsConstructor
@Slf4j
public class SensorEventController {

    private final SensorEventService sensorEventService;

    @PostMapping
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Received sensor event: {}", event);
        sensorEventService.processSensorEvent(event);
    }
}
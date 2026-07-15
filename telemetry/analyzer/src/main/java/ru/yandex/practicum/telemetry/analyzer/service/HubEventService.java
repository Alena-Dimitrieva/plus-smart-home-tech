package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ActionType;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.repository.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HubEventService {

    private final SensorRepository sensorRepository;
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    public void processHubEvent(HubEventAvro event) {
        String hubId = event.getHubId();
        Object payload = event.getPayload();

        switch (payload) {
            case DeviceAddedEventAvro added -> handleDeviceAdded(hubId, added);
            case DeviceRemovedEventAvro removed -> handleDeviceRemoved(hubId, removed);
            case ScenarioAddedEventAvro scenarioAdded -> handleScenarioAdded(hubId, scenarioAdded);
            case ScenarioRemovedEventAvro scenarioRemoved -> handleScenarioRemoved(hubId, scenarioRemoved);
            default -> log.warn("Неизвестный тип payload: {}", payload.getClass().getSimpleName());
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        Sensor sensor = new Sensor();
        sensor.setId(event.getId());
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
        log.info("Добавлен датчик: {} для хаба {}", sensor.getId(), hubId);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
        sensorRepository.findById(event.getId())
                .ifPresentOrElse(
                        sensor -> {
                            sensorRepository.delete(sensor);
                            log.info("Удалён датчик: {} для хаба {}", event.getId(), hubId);
                        },
                        () -> log.warn("Попытка удалить несуществующий датчик: {}", event.getId())
                );
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresent(s -> {
                    throw new RuntimeException("Сценарий с именем '" + event.getName() + "' уже существует для хаба " + hubId);
                });

        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(event.getName());
        scenario = scenarioRepository.save(scenario);

        for (ScenarioConditionAvro conditionAvro : event.getConditions()) {
            Condition condition = new Condition();
            condition.setType(ConditionType.valueOf(conditionAvro.getType().name()));
            condition.setOperation(ConditionOperation.valueOf(conditionAvro.getOperation().name()));

            // Извлекаем значение из oneof
            Object value = conditionAvro.getValue();
            if (value instanceof Boolean boolVal) {
                condition.setValue(boolVal ? 1 : 0);
            } else if (value instanceof Integer intVal) {
                condition.setValue(intVal);
            } else {
                throw new RuntimeException("Неизвестный тип значения условия: " + value);
            }
            condition = conditionRepository.save(condition);

            Sensor sensor = sensorRepository.findById(conditionAvro.getSensorId())
                    .orElseThrow(() -> new RuntimeException("Сенсор не найден: " + conditionAvro.getSensorId()));

            ScenarioCondition sc = new ScenarioCondition();
            sc.setScenario(scenario);
            sc.setSensor(sensor);
            sc.setCondition(condition);
            scenarioConditionRepository.save(sc);
        }

        for (DeviceActionAvro actionAvro : event.getActions()) {
            Action action = new Action();
            action.setType(ActionType.valueOf(actionAvro.getType().name()));
            action.setValue(actionAvro.getValue() != null ? actionAvro.getValue() : 0);
            action = actionRepository.save(action);

            Sensor sensor = sensorRepository.findById(actionAvro.getSensorId())
                    .orElseThrow(() -> new RuntimeException("Сенсор не найден: " + actionAvro.getSensorId()));

            ScenarioAction sa = new ScenarioAction();
            sa.setScenario(scenario);
            sa.setSensor(sensor);
            sa.setAction(action);
            scenarioActionRepository.save(sa);
        }

        log.info("Добавлен сценарий: {} для хаба {}", scenario.getName(), hubId);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        scenarioRepository.findByHubIdAndName(hubId, event.getName())
                .ifPresentOrElse(
                        scenario -> {
                            scenarioRepository.delete(scenario);
                            log.info("Удалён сценарий: {} для хаба {}", event.getName(), hubId);
                        },
                        () -> log.warn("Попытка удалить несуществующий сценарий: {} для хаба {}", event.getName(), hubId)
                );
    }
}

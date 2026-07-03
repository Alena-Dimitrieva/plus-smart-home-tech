package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.client.HubRouterClient;
import ru.yandex.practicum.telemetry.analyzer.model.*;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ConditionOperation;
import ru.yandex.practicum.telemetry.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient hubRouterClient;

    /**
     * Обрабатывает напшот: проверяет все сценарии для данного хаба
     * и отправляет действия для тех, чьи условия выполнены.
     */
    @Transactional
    public void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId();
        log.debug("Обработка снапшота для хаба: {}", hubId);

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.debug("Нет сценариев для хаба: {}", hubId);
            return;
        }

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        for (Scenario scenario : scenarios) {
            boolean conditionsMet = checkConditions(scenario, sensorsState);
            if (conditionsMet) {
                log.info("Сценарий '{}' активирован для хаба {}", scenario.getName(), hubId);
                // Отправляем действия в Hub Router
                sendActions(hubId, scenario);
            } else {
                log.debug("Сценарий '{}' не активирован", scenario.getName());
            }
        }
    }

    /**
     * Проверяет все условия сценария на основе текущего состояния датчиков.
     */
    private boolean checkConditions(Scenario scenario, Map<String, SensorStateAvro> sensorsState) {
        for (ScenarioCondition sc : scenario.getConditions()) {
            String sensorId = sc.getSensor().getId();
            SensorStateAvro state = sensorsState.get(sensorId);
            if (state == null) {
                log.debug("Нет данных для датчика {} в снапшоте", sensorId);
                return false;
            }
            if (!checkSingleCondition(sc.getCondition(), state.getData())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверяет одно условие: тип, операция, значение.
     */
    private boolean checkSingleCondition(Condition condition, Object sensorData) {
        ConditionType type = condition.getType();
        ConditionOperation operation = condition.getOperation();
        int expectedValue = condition.getValue();

        int actualValue = extractValue(type, sensorData);

        return switch (operation) {
            case EQUALS -> actualValue == expectedValue;
            case GREATER_THAN -> actualValue > expectedValue;
            case LOWER_THAN -> actualValue < expectedValue;
        };
    }

    /**
     * Извлекает числовое значение из Avro-объекта датчика по типу условия.
     */
    private int extractValue(ConditionType type, Object sensorData) {
        return switch (type) {
            case TEMPERATURE -> {
                if (sensorData instanceof TemperatureSensorAvro temp) {
                    yield temp.getTemperatureC();
                } else if (sensorData instanceof ClimateSensorAvro climate) {
                    yield climate.getTemperatureC();
                } else {
                    throw new IllegalArgumentException("Неверный тип датчика для температуры: " + sensorData.getClass());
                }
            }
            case HUMIDITY -> {
                if (sensorData instanceof ClimateSensorAvro climate) {
                    yield climate.getHumidity();
                } else {
                    throw new IllegalArgumentException("Неверный тип датчика для влажности: " + sensorData.getClass());
                }
            }
            case CO2LEVEL -> {
                if (sensorData instanceof ClimateSensorAvro climate) {
                    yield climate.getCo2Level();
                } else {
                    throw new IllegalArgumentException("Неверный тип датчика для CO2: " + sensorData.getClass());
                }
            }
            case LUMINOSITY -> {
                if (sensorData instanceof LightSensorAvro light) {
                    yield light.getLuminosity();
                } else {
                    throw new IllegalArgumentException("Неверный тип датчика для освещённости: " + sensorData.getClass());
                }
            }
            case MOTION -> {
                if (sensorData instanceof MotionSensorAvro motion) {
                    yield motion.getMotion() ? 1 : 0;
                } else {
                    throw new IllegalArgumentException("Неверный тип датчика для движения: " + sensorData.getClass());
                }
            }
            case SWITCH -> {
                if (sensorData instanceof SwitchSensorAvro sw) {
                    yield sw.getState() ? 1 : 0;
                } else {
                    throw new IllegalArgumentException("Неверный тип датчика для переключателя: " + sensorData.getClass());
                }
            }
        };
    }

    /**
     * Отправляет действия сценария в Hub Router через gRPC.
     */
    private void sendActions(String hubId, Scenario scenario) {
        for (ScenarioAction sa : scenario.getActions()) {
            Action action = sa.getAction();
            String sensorId = sa.getSensor().getId();
            hubRouterClient.sendAction(hubId, scenario.getName(), sensorId, action);
        }
    }
}

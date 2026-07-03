package ru.yandex.practicum.telemetry.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.telemetry.analyzer.model.ScenarioCondition;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, Long> {
}

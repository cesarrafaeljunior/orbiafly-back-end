package com.orbiafly.api.modules.financialprojection.application.model;

import com.orbiafly.api.modules.financialprojection.domain.ProjectionEventDirection;
import com.orbiafly.api.modules.financialprojection.domain.ProjectionSourceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProjectionEventResult(
    LocalDate date,
    String description,
    BigDecimal amount,
    ProjectionEventDirection direction,
    ProjectionSourceType sourceType,
    UUID sourceId,
    BigDecimal projectedBalanceAfterEvent) {
}

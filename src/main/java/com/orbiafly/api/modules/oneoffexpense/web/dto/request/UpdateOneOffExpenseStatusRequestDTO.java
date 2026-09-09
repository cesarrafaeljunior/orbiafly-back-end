package com.orbiafly.api.modules.oneoffexpense.web.dto.request;

import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOneOffExpenseStatusRequestDTO(@NotNull PaymentStatus status) {
}

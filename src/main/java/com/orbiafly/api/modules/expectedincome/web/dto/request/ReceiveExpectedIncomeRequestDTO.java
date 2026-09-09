package com.orbiafly.api.modules.expectedincome.web.dto.request;

import java.time.LocalDate;
import java.util.UUID;

public record ReceiveExpectedIncomeRequestDTO(
    UUID financialAccountId,
    LocalDate receivedDate) {
}

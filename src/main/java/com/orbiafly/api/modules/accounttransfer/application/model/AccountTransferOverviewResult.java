package com.orbiafly.api.modules.accounttransfer.application.model;

import java.math.BigDecimal;
import java.util.List;

public record AccountTransferOverviewResult(
    BigDecimal transferredTotal,
    int transferCount,
    List<AccountTransferResult> items) {
}

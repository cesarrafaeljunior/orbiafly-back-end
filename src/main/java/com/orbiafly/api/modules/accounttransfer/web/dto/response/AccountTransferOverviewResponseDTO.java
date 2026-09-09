package com.orbiafly.api.modules.accounttransfer.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AccountTransferOverviewResponseDTO(
    BigDecimal transferredTotal,
    int transferCount,
    List<AccountTransferResponseDTO> items) {
}

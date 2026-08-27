package com.orbiafly.api.modules.creditcard.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CreditCardOverviewResponseDTO(
    BigDecimal totalLimit,
    BigDecimal usedLimit,
    BigDecimal availableLimit,
    int activeCount,
    List<CreditCardResponseDTO> cards) {
}

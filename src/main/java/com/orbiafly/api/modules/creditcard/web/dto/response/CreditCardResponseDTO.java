package com.orbiafly.api.modules.creditcard.web.dto.response;

import com.orbiafly.api.modules.creditcard.domain.CreditCardBrand;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditCardResponseDTO(
    UUID id,
    String name,
    CreditCardBrand brand,
    String lastFourDigits,
    BigDecimal totalLimit,
    BigDecimal usedLimit,
    BigDecimal availableLimit,
    int closingDay,
    int dueDay,
    boolean active,
    Instant deletedAt,
    Instant createdAt,
    Instant updatedAt) {
}

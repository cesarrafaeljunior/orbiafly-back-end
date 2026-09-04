package com.orbiafly.api.modules.creditcard.application.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardResult;
import com.orbiafly.api.modules.creditcard.domain.CreditCard;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class CreditCardResultFactory {

  public CreditCardResult createCreditCardResult(CreditCard card, BigDecimal usedLimit) {
    return new CreditCardResult(
        card.getId(),
        card.getName(),
        card.getBrand(),
        card.getLastFourDigits(),
        card.getTotalLimit(),
        usedLimit,
        card.getTotalLimit().subtract(usedLimit),
        card.getClosingDay(),
        card.getDueDay(),
        card.isActive(),
        card.getDeletedAt(),
        card.getCreatedAt(),
        card.getUpdatedAt());
  }
}

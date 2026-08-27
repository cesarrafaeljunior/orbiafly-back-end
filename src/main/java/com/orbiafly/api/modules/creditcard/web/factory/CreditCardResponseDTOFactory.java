package com.orbiafly.api.modules.creditcard.web.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardResult;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CreditCardResponseDTOFactory {

  public CreditCardResponseDTO createCreditCardResponseDTO(CreditCardResult card) {
    return new CreditCardResponseDTO(
        card.id(),
        card.name(),
        card.brand(),
        card.lastFourDigits(),
        card.totalLimit(),
        card.usedLimit(),
        card.availableLimit(),
        card.closingDay(),
        card.dueDay(),
        card.active(),
        card.deletedAt(),
        card.createdAt(),
        card.updatedAt());
  }
}

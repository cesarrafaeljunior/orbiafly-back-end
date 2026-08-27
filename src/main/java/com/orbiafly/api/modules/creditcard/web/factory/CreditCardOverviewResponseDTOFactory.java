package com.orbiafly.api.modules.creditcard.web.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardOverviewResult;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardOverviewResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CreditCardOverviewResponseDTOFactory {

  private final CreditCardResponseDTOFactory cardFactory;

  public CreditCardOverviewResponseDTOFactory(CreditCardResponseDTOFactory cardFactory) {
    this.cardFactory = cardFactory;
  }

  public CreditCardOverviewResponseDTO createCreditCardOverviewResponseDTO(
      CreditCardOverviewResult overview) {
    return new CreditCardOverviewResponseDTO(
        overview.totalLimit(),
        overview.usedLimit(),
        overview.availableLimit(),
        overview.activeCount(),
        overview.cards().stream()
            .map(cardFactory::createCreditCardResponseDTO)
            .toList());
  }
}

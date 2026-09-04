package com.orbiafly.api.modules.creditcard.web.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardPurchaseOverviewResult;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseOverviewResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CreditCardPurchaseOverviewResponseDTOFactory {

  private final CreditCardPurchaseResponseDTOFactory purchaseFactory;

  public CreditCardPurchaseOverviewResponseDTOFactory(
      CreditCardPurchaseResponseDTOFactory purchaseFactory) {
    this.purchaseFactory = purchaseFactory;
  }

  public CreditCardPurchaseOverviewResponseDTO createCreditCardPurchaseOverviewResponseDTO(
      CreditCardPurchaseOverviewResult overview) {
    return new CreditCardPurchaseOverviewResponseDTO(
        overview.totalCommitted(),
        overview.activePurchaseCount(),
        overview.futureInstallmentCount(),
        overview.currentMonthInstallmentsAmount(),
        overview.purchases().stream()
            .map(purchaseFactory::createCreditCardPurchaseResponseDTO)
            .toList());
  }
}

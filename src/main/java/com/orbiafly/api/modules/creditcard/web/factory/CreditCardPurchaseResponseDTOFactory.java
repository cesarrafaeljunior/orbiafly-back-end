package com.orbiafly.api.modules.creditcard.web.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardPurchaseResult;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CreditCardPurchaseResponseDTOFactory {

  private final CreditCardInstallmentResponseDTOFactory installmentFactory;

  public CreditCardPurchaseResponseDTOFactory(
      CreditCardInstallmentResponseDTOFactory installmentFactory) {
    this.installmentFactory = installmentFactory;
  }

  public CreditCardPurchaseResponseDTO createCreditCardPurchaseResponseDTO(
      CreditCardPurchaseResult purchase) {
    return new CreditCardPurchaseResponseDTO(
        purchase.id(),
        purchase.description(),
        purchase.totalAmount(),
        purchase.purchaseDate(),
        purchase.installmentCount(),
        purchase.installmentAmount(),
        purchase.creditCardId(),
        purchase.creditCardName(),
        purchase.creditCardBrand(),
        purchase.creditCardLastFourDigits(),
        purchase.status(),
        purchase.completedInstallmentCount(),
        purchase.futureInstallmentCount(),
        purchase.nextInstallment() == null
            ? null
            : installmentFactory.createCreditCardInstallmentResponseDTO(purchase.nextInstallment()),
        purchase.installments().stream()
            .map(installmentFactory::createCreditCardInstallmentResponseDTO)
            .toList(),
        purchase.deletedAt(),
        purchase.createdAt(),
        purchase.updatedAt());
  }
}

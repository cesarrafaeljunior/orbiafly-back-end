package com.orbiafly.api.modules.creditcard.web.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardMonthlyPurchaseInstallmentResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardMonthlyPurchasesResult;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchaseInstallmentResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchaseSummaryResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchasesResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CreditCardMonthlyPurchasesResponseDTOFactory {

  public CreditCardMonthlyPurchasesResponseDTO createCreditCardMonthlyPurchasesResponseDTO(
      CreditCardMonthlyPurchasesResult result) {
    return new CreditCardMonthlyPurchasesResponseDTO(
        result.year(),
        result.month(),
        new CreditCardMonthlyPurchaseSummaryResponseDTO(
            result.summary().total(),
            result.summary().paidTotal(),
            result.summary().pendingTotal(),
            result.summary().totalCount(),
            result.summary().paidCount(),
            result.summary().pendingCount(),
            result.summary().paidPercentage()),
        result.items().stream()
            .map(this::createCreditCardMonthlyPurchaseInstallmentResponseDTO)
            .toList());
  }

  public CreditCardMonthlyPurchaseInstallmentResponseDTO createCreditCardMonthlyPurchaseInstallmentResponseDTO(
      CreditCardMonthlyPurchaseInstallmentResult item) {
    return new CreditCardMonthlyPurchaseInstallmentResponseDTO(
        item.installmentId(),
        item.purchaseId(),
        item.description(),
        item.amount(),
        item.totalAmount(),
        item.purchaseDate(),
        item.installmentNumber(),
        item.installmentCount(),
        item.creditCardId(),
        item.creditCardName(),
        item.creditCardBrand(),
        item.creditCardLastFourDigits(),
        item.competenceYear(),
        item.competenceMonth(),
        item.dueDate(),
        item.status(),
        item.paidAt(),
        item.paidInstallmentCount(),
        item.pendingInstallmentCount(),
        item.remainingInstallmentCount(),
        item.endingYear(),
        item.endingMonth(),
        item.monthsUntilEnd(),
        item.createdAt(),
        item.updatedAt());
  }
}

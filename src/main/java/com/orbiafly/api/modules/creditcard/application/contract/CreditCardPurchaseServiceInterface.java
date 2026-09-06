package com.orbiafly.api.modules.creditcard.application.contract;

import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallmentStatus;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchaseInstallmentResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardMonthlyPurchasesResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditCardPurchaseServiceInterface {

  CreditCardPurchaseOverviewResponseDTO listCreditCardPurchases();

  CreditCardMonthlyPurchasesResponseDTO getMonthlyCreditCardPurchases(int year, int month);

  CreditCardPurchaseResponseDTO getCreditCardPurchase(UUID id);

  CreditCardPurchaseResponseDTO createCreditCardPurchase(
      String description,
      UUID creditCardId,
      BigDecimal totalAmount,
      LocalDate purchaseDate,
      int installmentCount);

  CreditCardMonthlyPurchaseInstallmentResponseDTO updateCreditCardInstallmentAmount(
      UUID installmentId,
      BigDecimal amount);

  CreditCardMonthlyPurchaseInstallmentResponseDTO updateCreditCardInstallmentStatus(
      UUID installmentId,
      CreditCardInstallmentStatus status);

  void deleteCreditCardPurchase(UUID id);
}

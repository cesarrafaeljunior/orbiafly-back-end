package com.orbiafly.api.modules.creditcard.application.contract;

import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardPurchaseResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CreditCardPurchaseServiceInterface {

  CreditCardPurchaseOverviewResponseDTO listCreditCardPurchases();

  CreditCardPurchaseResponseDTO getCreditCardPurchase(UUID id);

  CreditCardPurchaseResponseDTO createCreditCardPurchase(
      String description,
      UUID creditCardId,
      BigDecimal totalAmount,
      LocalDate purchaseDate,
      int installmentCount);

  void deleteCreditCardPurchase(UUID id);
}

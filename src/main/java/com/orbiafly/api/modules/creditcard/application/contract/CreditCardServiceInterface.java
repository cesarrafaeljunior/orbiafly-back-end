package com.orbiafly.api.modules.creditcard.application.contract;

import com.orbiafly.api.modules.creditcard.domain.CreditCardBrand;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardOverviewResponseDTO;
import com.orbiafly.api.modules.creditcard.web.dto.response.CreditCardResponseDTO;
import java.math.BigDecimal;
import java.util.UUID;

public interface CreditCardServiceInterface {

  CreditCardOverviewResponseDTO listCreditCards(boolean includeInactive);

  CreditCardResponseDTO getCreditCard(UUID id);

  CreditCardResponseDTO createCreditCard(
      String name,
      CreditCardBrand brand,
      String lastFourDigits,
      BigDecimal totalLimit,
      int closingDay,
      int dueDay);

  CreditCardResponseDTO updateCreditCard(
      UUID id,
      String name,
      CreditCardBrand brand,
      String lastFourDigits,
      BigDecimal totalLimit,
      int closingDay,
      int dueDay);

  CreditCardResponseDTO deactivateCreditCard(UUID id);

  CreditCardResponseDTO reactivateCreditCard(UUID id);

  void deleteCreditCard(UUID id);
}

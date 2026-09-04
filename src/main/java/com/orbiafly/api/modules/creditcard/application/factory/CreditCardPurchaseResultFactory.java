package com.orbiafly.api.modules.creditcard.application.factory;

import com.orbiafly.api.modules.creditcard.application.model.CreditCardInstallmentResult;
import com.orbiafly.api.modules.creditcard.application.model.CreditCardPurchaseResult;
import com.orbiafly.api.modules.creditcard.domain.CreditCardInstallment;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchase;
import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseStatus;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CreditCardPurchaseResultFactory {

  private final CreditCardInstallmentResultFactory installmentResultFactory;

  public CreditCardPurchaseResultFactory(
      CreditCardInstallmentResultFactory installmentResultFactory) {
    this.installmentResultFactory = installmentResultFactory;
  }

  public CreditCardPurchaseResult createCreditCardPurchaseResult(
      CreditCardPurchase purchase,
      YearMonth currentCompetence) {
    List<CreditCardInstallment> installments = purchase.getInstallments().stream()
        .sorted(Comparator.comparingInt(CreditCardInstallment::getInstallmentNumber))
        .toList();
    List<CreditCardInstallmentResult> installmentResults = installments.stream()
        .map(installmentResultFactory::createCreditCardInstallmentResult)
        .toList();
    List<CreditCardInstallment> futureInstallments = installments.stream()
        .filter(installment -> !YearMonth.of(
            installment.getCompetenceYear(),
            installment.getCompetenceMonth()).isBefore(currentCompetence))
        .toList();
    CreditCardInstallmentResult nextInstallment = futureInstallments.stream()
        .findFirst()
        .map(installmentResultFactory::createCreditCardInstallmentResult)
        .orElse(null);
    int futureCount = futureInstallments.size();

    return new CreditCardPurchaseResult(
        purchase.getId(),
        purchase.getDescription(),
        purchase.getTotalAmount(),
        purchase.getPurchaseDate(),
        purchase.getInstallmentCount(),
        purchase.getInstallmentAmount(),
        purchase.getCreditCard().getId(),
        purchase.getCreditCard().getName(),
        purchase.getCreditCard().getBrand(),
        purchase.getCreditCard().getLastFourDigits(),
        futureCount > 0 ? CreditCardPurchaseStatus.EM_ANDAMENTO : CreditCardPurchaseStatus.FINALIZADA,
        purchase.getInstallmentCount() - futureCount,
        futureCount,
        nextInstallment,
        installmentResults,
        purchase.getDeletedAt(),
        purchase.getCreatedAt(),
        purchase.getUpdatedAt());
  }
}

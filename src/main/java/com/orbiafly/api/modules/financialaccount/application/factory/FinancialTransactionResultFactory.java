package com.orbiafly.api.modules.financialaccount.application.factory;

import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransaction;
import com.orbiafly.api.modules.financialaccount.domain.FinancialTransactionType;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class FinancialTransactionResultFactory {

  public FinancialTransactionResult createFinancialTransactionResult(
      FinancialTransaction transaction) {
    return new FinancialTransactionResult(
        transaction.getId(),
        transaction.getFinancialAccount().getId(),
        transaction.getType(),
        transaction.getAmount(),
        balanceEffect(transaction),
        transaction.getDescription(),
        transaction.getOccurredOn(),
        transaction.getReferenceType(),
        transaction.getReferenceId(),
        transaction.getReversalOf() == null ? null : transaction.getReversalOf().getId(),
        transaction.getCreatedAt());
  }

  private BigDecimal balanceEffect(FinancialTransaction transaction) {
    return switch (transaction.getType()) {
      case CREDIT -> transaction.getAmount();
      case DEBIT -> transaction.getAmount().negate();
      case ADJUSTMENT -> transaction.getAmount();
    };
  }
}

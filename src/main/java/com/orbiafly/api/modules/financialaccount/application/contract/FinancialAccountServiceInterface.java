package com.orbiafly.api.modules.financialaccount.application.contract;

import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialAccountResult;
import com.orbiafly.api.modules.financialaccount.application.model.FinancialTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialAccountType;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.financialaccount.web.dto.response.AccountTransactionResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialAccountsOverviewResponseDTO;
import com.orbiafly.api.modules.financialaccount.web.dto.response.FinancialTransactionResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface FinancialAccountServiceInterface {

  FinancialAccountsOverviewResponseDTO listFinancialAccounts(boolean includeInactive);

  FinancialAccountResponseDTO getFinancialAccount(UUID id);

  FinancialAccountResult getFinancialAccountResult(UUID id);

  FinancialAccountResponseDTO createFinancialAccount(
      String name,
      FinancialAccountType type,
      String institution,
      BigDecimal openingBalance);

  FinancialAccountResponseDTO updateFinancialAccount(
      UUID id,
      String name,
      FinancialAccountType type,
      String institution);

  FinancialAccountResponseDTO deactivateFinancialAccount(UUID id);

  FinancialAccountResponseDTO reactivateFinancialAccount(UUID id);

  void deleteFinancialAccount(UUID id);

  List<FinancialTransactionResponseDTO> listFinancialAccountTransactions(UUID accountId);

  AccountTransactionResponseDTO adjustFinancialAccountBalance(
      UUID accountId,
      BigDecimal targetBalance,
      String description);

  AccountTransactionResult debitFinancialAccountForReference(
      UUID accountId,
      BigDecimal amount,
      String description,
      LocalDate occurredOn,
      FinancialReferenceType referenceType,
      UUID referenceId);

  AccountTransactionResult reverseFinancialAccountDebit(
      UUID transactionId, LocalDate occurredOn, String description);

  FinancialTransactionResult getFinancialTransaction(UUID id);

  boolean hasActiveFinancialAccounts();

  Map<UUID, String> findFinancialAccountNames(Set<UUID> ids);
}

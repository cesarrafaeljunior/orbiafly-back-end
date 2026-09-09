package com.orbiafly.api.modules.expectedincome.application.contract;

import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeOverviewResponseDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeReceiptResponseDTO;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface ExpectedIncomeServiceInterface {

  ExpectedIncomeOverviewResponseDTO listExpectedIncomes();

  ExpectedIncomeResponseDTO getExpectedIncome(UUID id);

  ExpectedIncomeResponseDTO createExpectedIncome(
      String description,
      BigDecimal amount,
      LocalDate expectedDate,
      UUID financialAccountId,
      String notes);

  ExpectedIncomeResponseDTO updateExpectedIncome(
      UUID id,
      String description,
      BigDecimal amount,
      LocalDate expectedDate,
      UUID financialAccountId,
      String notes);

  ExpectedIncomeReceiptResponseDTO receiveExpectedIncome(
      UUID id,
      UUID financialAccountId,
      LocalDate receivedDate);

  ExpectedIncomeResponseDTO cancelExpectedIncome(UUID id);
}

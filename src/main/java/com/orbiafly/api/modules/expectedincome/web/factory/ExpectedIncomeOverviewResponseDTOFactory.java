package com.orbiafly.api.modules.expectedincome.web.factory;

import com.orbiafly.api.modules.expectedincome.application.model.ExpectedIncomeOverviewResult;
import com.orbiafly.api.modules.expectedincome.web.dto.response.ExpectedIncomeOverviewResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ExpectedIncomeOverviewResponseDTOFactory {

  private final ExpectedIncomeResponseDTOFactory responseDTOFactory;

  public ExpectedIncomeOverviewResponseDTOFactory(ExpectedIncomeResponseDTOFactory responseDTOFactory) {
    this.responseDTOFactory = responseDTOFactory;
  }

  public ExpectedIncomeOverviewResponseDTO createExpectedIncomeOverviewResponseDTO(
      ExpectedIncomeOverviewResult result) {
    return new ExpectedIncomeOverviewResponseDTO(
        result.pendingTotal(),
        result.receivedTotal(),
        result.canceledTotal(),
        result.pendingCount(),
        result.receivedCount(),
        result.canceledCount(),
        result.items().stream()
            .map(responseDTOFactory::createExpectedIncomeResponseDTO)
            .toList());
  }
}

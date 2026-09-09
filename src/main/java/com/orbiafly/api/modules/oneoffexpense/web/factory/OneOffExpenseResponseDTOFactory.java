package com.orbiafly.api.modules.oneoffexpense.web.factory;

import com.orbiafly.api.modules.oneoffexpense.application.model.OneOffExpenseResult;
import com.orbiafly.api.modules.oneoffexpense.web.dto.response.OneOffExpenseResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class OneOffExpenseResponseDTOFactory {

  public OneOffExpenseResponseDTO createOneOffExpenseResponseDTO(OneOffExpenseResult result) {
    return new OneOffExpenseResponseDTO(
        result.id(), result.name(), result.amount(), result.expenseDate(), result.categoryId(),
        result.category(), result.status(), result.paidAt(), result.financialAccountId(),
        result.financialAccountName(), result.overdue(), result.daysUntilDue(),
        result.daysOverdue(), result.createdAt(), result.updatedAt());
  }
}

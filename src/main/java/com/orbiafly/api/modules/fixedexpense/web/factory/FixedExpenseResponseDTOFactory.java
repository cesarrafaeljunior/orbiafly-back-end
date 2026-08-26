package com.orbiafly.api.modules.fixedexpense.web.factory;

import com.orbiafly.api.modules.fixedexpense.application.model.FixedExpenseResult;
import com.orbiafly.api.modules.fixedexpense.web.dto.response.FixedExpenseResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class FixedExpenseResponseDTOFactory {

  public FixedExpenseResponseDTO createFixedExpenseResponseDTO(FixedExpenseResult expense) {
    return new FixedExpenseResponseDTO(
        expense.id(),
        expense.name(),
        expense.defaultAmount(),
        expense.dueDay(),
        expense.categoryId(),
        expense.category(),
        expense.active(),
        expense.createdAt(),
        expense.updatedAt());
  }
}

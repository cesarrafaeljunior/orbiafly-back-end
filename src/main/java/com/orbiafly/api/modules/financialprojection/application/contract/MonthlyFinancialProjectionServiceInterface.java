package com.orbiafly.api.modules.financialprojection.application.contract;

import com.orbiafly.api.modules.financialprojection.application.model.MonthlyFinancialProjectionResult;
import com.orbiafly.api.modules.financialprojection.web.dto.response.MonthlyFinancialProjectionResponseDTO;

public interface MonthlyFinancialProjectionServiceInterface {

  MonthlyFinancialProjectionResponseDTO getMonthlyProjection(int year, int month);

  MonthlyFinancialProjectionResult getMonthlyProjectionResult(int year, int month);
}

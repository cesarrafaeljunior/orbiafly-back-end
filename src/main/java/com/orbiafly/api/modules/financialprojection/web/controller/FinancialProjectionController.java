package com.orbiafly.api.modules.financialprojection.web.controller;

import com.orbiafly.api.modules.financialprojection.application.contract.MonthlyFinancialProjectionServiceInterface;
import com.orbiafly.api.modules.financialprojection.web.dto.response.MonthlyFinancialProjectionResponseDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/financial-projections")
public class FinancialProjectionController {

  private final MonthlyFinancialProjectionServiceInterface service;

  public FinancialProjectionController(MonthlyFinancialProjectionServiceInterface service) {
    this.service = service;
  }

  @GetMapping("/monthly")
  public MonthlyFinancialProjectionResponseDTO getMonthlyProjection(
      @RequestParam @Min(2000) @Max(2200) int year,
      @RequestParam @Min(1) @Max(12) int month) {
    return service.getMonthlyProjection(year, month);
  }
}

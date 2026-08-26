package com.orbiafly.api.modules.dashboard.web.controller;

import com.orbiafly.api.modules.dashboard.application.contract.DashboardServiceInterface;
import com.orbiafly.api.modules.dashboard.web.dto.response.DashboardMonthlyResponseDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

  private final DashboardServiceInterface service;

  public DashboardController(DashboardServiceInterface service) {
    this.service = service;
  }

  @GetMapping("/monthly")
  public DashboardMonthlyResponseDTO getMonthlyDashboard(
      @RequestParam @Min(2000) @Max(2200) int year,
      @RequestParam @Min(1) @Max(12) int month) {
    return service.getMonthlyDashboard(year, month);
  }
}

package com.orbiafly.api.modules.dashboard.application.contract;

import com.orbiafly.api.modules.dashboard.web.dto.response.DashboardMonthlyResponseDTO;

public interface DashboardServiceInterface {

  DashboardMonthlyResponseDTO getMonthlyDashboard(int year, int month);
}

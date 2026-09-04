package com.orbiafly.api.modules.loan.web.dto.request;

import com.orbiafly.api.modules.loan.domain.LoanDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLoanRequestDTO(
    @NotBlank @Size(max = 140) String name,
    @NotNull LoanDirection direction,
    @NotBlank @Size(max = 140) String counterparty,
    @NotNull @DecimalMin("0.01") BigDecimal totalAmount,
    @NotNull LocalDate startDate,
    @Min(1) @Max(360) Integer installmentCount,
    @DecimalMin("0.01") BigDecimal installmentAmount,
    LocalDate firstDueDate,
    @Size(max = 60) String interestRate,
    @Size(max = 500) String notes) {
}

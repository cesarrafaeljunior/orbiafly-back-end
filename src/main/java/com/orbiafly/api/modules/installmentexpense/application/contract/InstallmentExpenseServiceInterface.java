package com.orbiafly.api.modules.installmentexpense.application.contract;

import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPaymentResponseDTO;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPurchaseResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InstallmentExpenseServiceInterface {

  List<InstallmentPurchaseResponseDTO> listInstallmentPurchases();

  InstallmentPurchaseResponseDTO getInstallmentPurchase(UUID id);

  InstallmentPurchaseResponseDTO createInstallmentPurchase(
      String description,
      BigDecimal totalAmount,
      int installmentCount,
      LocalDate firstInstallmentDate);

  InstallmentPaymentResponseDTO payInstallment(
      UUID installmentId,
      UUID financialAccountId,
      LocalDate paymentDate);
}

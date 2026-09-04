package com.orbiafly.api.modules.installmentexpense.application.service;

import com.orbiafly.api.infrastructure.security.CurrentUserProvider;
import com.orbiafly.api.modules.financialaccount.application.contract.FinancialAccountServiceInterface;
import com.orbiafly.api.modules.financialaccount.application.model.AccountTransactionResult;
import com.orbiafly.api.modules.financialaccount.domain.FinancialReferenceType;
import com.orbiafly.api.modules.fixedexpense.application.ResourceNotFoundException;
import com.orbiafly.api.modules.fixedexpense.domain.PaymentStatus;
import com.orbiafly.api.modules.installmentexpense.application.contract.InstallmentExpenseServiceInterface;
import com.orbiafly.api.modules.installmentexpense.application.factory.InstallmentExpenseResultFactory;
import com.orbiafly.api.modules.installmentexpense.application.model.InstallmentPaymentResult;
import com.orbiafly.api.modules.installmentexpense.domain.InstallmentExpenseInstallment;
import com.orbiafly.api.modules.installmentexpense.domain.InstallmentPurchase;
import com.orbiafly.api.modules.installmentexpense.infrastructure.persistence.InstallmentExpenseInstallmentRepositoryInterface;
import com.orbiafly.api.modules.installmentexpense.infrastructure.persistence.InstallmentPurchaseRepositoryInterface;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPaymentResponseDTO;
import com.orbiafly.api.modules.installmentexpense.web.dto.response.InstallmentPurchaseResponseDTO;
import com.orbiafly.api.modules.installmentexpense.web.factory.InstallmentExpenseResponseDTOFactory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InstallmentExpenseServiceImpl implements InstallmentExpenseServiceInterface {

  private final InstallmentPurchaseRepositoryInterface purchaseRepository;
  private final InstallmentExpenseInstallmentRepositoryInterface installmentRepository;
  private final FinancialAccountServiceInterface financialAccountService;
  private final CurrentUserProvider currentUserProvider;
  private final InstallmentExpenseResultFactory resultFactory;
  private final InstallmentExpenseResponseDTOFactory responseDTOFactory;
  private final Clock clock;

  public InstallmentExpenseServiceImpl(
      InstallmentPurchaseRepositoryInterface purchaseRepository,
      InstallmentExpenseInstallmentRepositoryInterface installmentRepository,
      FinancialAccountServiceInterface financialAccountService,
      CurrentUserProvider currentUserProvider,
      InstallmentExpenseResultFactory resultFactory,
      InstallmentExpenseResponseDTOFactory responseDTOFactory,
      Clock clock) {
    this.purchaseRepository = purchaseRepository;
    this.installmentRepository = installmentRepository;
    this.financialAccountService = financialAccountService;
    this.currentUserProvider = currentUserProvider;
    this.resultFactory = resultFactory;
    this.responseDTOFactory = responseDTOFactory;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public List<InstallmentPurchaseResponseDTO> listInstallmentPurchases() {
    return purchaseRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(
            currentUserProvider.currentUserId())
        .stream()
        .map(resultFactory::createInstallmentPurchaseResult)
        .map(responseDTOFactory::createInstallmentPurchaseResponseDTO)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public InstallmentPurchaseResponseDTO getInstallmentPurchase(UUID id) {
    return responseDTOFactory.createInstallmentPurchaseResponseDTO(
        resultFactory.createInstallmentPurchaseResult(findPurchase(id)));
  }

  @Override
  @Transactional
  public InstallmentPurchaseResponseDTO createInstallmentPurchase(
      String description,
      BigDecimal totalAmount,
      int installmentCount,
      LocalDate firstInstallmentDate) {
    UUID ownerUserId = currentUserProvider.currentUserId();
    Instant now = clock.instant();
    InstallmentPurchase purchase = new InstallmentPurchase(
        ownerUserId,
        description.trim(),
        totalAmount.setScale(2, RoundingMode.HALF_UP),
        installmentCount,
        firstInstallmentDate,
        now);
    List<BigDecimal> amounts = distributeInstallments(purchase.getTotalAmount(), installmentCount);
    for (int index = 0; index < installmentCount; index++) {
      purchase.addInstallment(new InstallmentExpenseInstallment(
          purchase,
          ownerUserId,
          index + 1,
          amounts.get(index),
          firstInstallmentDate.plusMonths(index),
          now));
    }
    return responseDTOFactory.createInstallmentPurchaseResponseDTO(
        resultFactory.createInstallmentPurchaseResult(purchaseRepository.save(purchase)));
  }

  @Override
  @Transactional
  public InstallmentPaymentResponseDTO payInstallment(
      UUID installmentId,
      UUID financialAccountId,
      LocalDate paymentDate) {
    InstallmentExpenseInstallment installment = findInstallmentForUpdate(installmentId);
    if (installment.getStatus() == PaymentStatus.PAID) {
      throw new IllegalStateException("Esta parcela já foi paga.");
    }
    AccountTransactionResult transaction = financialAccountService.debitFinancialAccountForReference(
        financialAccountId,
        installment.getAmount(),
        "Pagamento parcela " + installment.getInstallmentNumber() + " - "
            + installment.getPurchase().getDescription(),
        paymentDate,
        FinancialReferenceType.INSTALLMENT_EXPENSE_INSTALLMENT,
        installment.getId());
    installment.registerPayment(financialAccountId, transaction.transaction().id(), clock.instant());
    InstallmentPaymentResult result = new InstallmentPaymentResult(
        resultFactory.createInstallmentResult(installment),
        transaction.transaction(),
        transaction.financialAccount());
    return responseDTOFactory.createInstallmentPaymentResponseDTO(result);
  }

  private InstallmentPurchase findPurchase(UUID id) {
    return purchaseRepository.findByIdAndOwnerUserId(id, currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Compra parcelada não encontrada."));
  }

  private InstallmentExpenseInstallment findInstallmentForUpdate(UUID id) {
    return installmentRepository.findByIdAndOwnerUserIdForUpdate(
            id,
            currentUserProvider.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Parcela não encontrada."));
  }

  private List<BigDecimal> distributeInstallments(BigDecimal totalAmount, int installmentCount) {
    long totalCents = totalAmount.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    long baseCents = totalCents / installmentCount;
    long remainder = totalCents % installmentCount;
    return java.util.stream.IntStream.range(0, installmentCount)
        .mapToObj(index -> BigDecimal.valueOf(baseCents + (index < remainder ? 1 : 0), 2))
        .toList();
  }
}

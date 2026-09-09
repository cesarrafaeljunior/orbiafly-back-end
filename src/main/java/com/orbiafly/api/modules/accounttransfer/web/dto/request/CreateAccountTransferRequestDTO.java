package com.orbiafly.api.modules.accounttransfer.web.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateAccountTransferRequestDTO(
    @NotNull(message = "Informe a conta de origem.")
    UUID sourceAccountId,

    @NotNull(message = "Informe a conta de destino.")
    UUID destinationAccountId,

    @NotNull(message = "Informe o valor.")
    @DecimalMin(value = "0.01", message = "Informe um valor maior que zero.")
    BigDecimal amount,

    @NotNull(message = "Informe a data da transferência.")
    LocalDate transferDate,

    @Size(max = 180, message = "A descrição deve ter no máximo 180 caracteres.")
    String description) {
}

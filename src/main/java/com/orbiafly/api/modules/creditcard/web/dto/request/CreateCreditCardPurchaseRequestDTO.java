package com.orbiafly.api.modules.creditcard.web.dto.request;

import com.orbiafly.api.modules.creditcard.domain.CreditCardPurchaseType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCreditCardPurchaseRequestDTO(
    @NotBlank(message = "Informe a descrição da compra.")
    @Size(max = 160, message = "A descrição deve possuir no máximo 160 caracteres.")
    String description,

    @NotNull(message = "Selecione o cartão.")
    UUID creditCardId,

    @NotNull(message = "Informe o valor total.")
    @DecimalMin(value = "0.01", message = "O valor total deve ser maior que zero.")
    @Digits(integer = 13, fraction = 2, message = "Informe um valor válido com até duas casas decimais.")
    BigDecimal totalAmount,

    @NotNull(message = "Informe a data da compra.")
    LocalDate purchaseDate,

    @NotNull(message = "Informe o tipo da compra.")
    CreditCardPurchaseType purchaseType,

    UUID financialAccountId,

    @Min(value = 1, message = "A compra deve ter pelo menos uma parcela.")
    @Max(value = 360, message = "A compra deve ter no máximo 360 parcelas.")
    Integer installmentCount) {
}

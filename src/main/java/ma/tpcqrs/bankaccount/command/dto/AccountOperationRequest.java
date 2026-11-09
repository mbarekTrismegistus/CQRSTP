package ma.tpcqrs.bankaccount.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AccountOperationRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency
) {
}

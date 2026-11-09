package ma.tpcqrs.bankaccount.command.dto;

import jakarta.validation.constraints.NotNull;
import ma.tpcqrs.bankaccount.common.AccountStatus;

public record UpdateAccountStatusRequest(@NotNull AccountStatus status) {
}

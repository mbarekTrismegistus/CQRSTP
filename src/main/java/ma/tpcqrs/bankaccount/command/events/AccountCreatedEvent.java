package ma.tpcqrs.bankaccount.command.events;

import ma.tpcqrs.bankaccount.common.AccountStatus;

import java.math.BigDecimal;

public record AccountCreatedEvent(
        String accountId,
        BigDecimal initialBalance,
        String currency,
        AccountStatus status
) {
}

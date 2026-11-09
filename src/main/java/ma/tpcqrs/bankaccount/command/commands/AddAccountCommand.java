package ma.tpcqrs.bankaccount.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

public record AddAccountCommand(
        @TargetAggregateIdentifier String accountId,
        BigDecimal initialBalance,
        String currency
) {
}

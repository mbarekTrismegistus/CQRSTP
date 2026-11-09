package ma.tpcqrs.bankaccount.command.commands;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

public record DebitAccountCommand(
        @TargetAggregateIdentifier String accountId,
        BigDecimal amount,
        String currency
) {
}

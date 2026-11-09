package ma.tpcqrs.bankaccount.command.commands;

import ma.tpcqrs.bankaccount.common.AccountStatus;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record UpdateAccountStatusCommand(
        @TargetAggregateIdentifier String accountId,
        AccountStatus status
) {
}

package ma.tpcqrs.bankaccount.command.events;

import ma.tpcqrs.bankaccount.common.AccountStatus;

public record AccountStatusUpdatedEvent(String accountId, AccountStatus status) {
}

package ma.tpcqrs.bankaccount.command.events;

import ma.tpcqrs.bankaccount.common.AccountStatus;

public record AccountActivatedEvent(String accountId, AccountStatus status) {
}

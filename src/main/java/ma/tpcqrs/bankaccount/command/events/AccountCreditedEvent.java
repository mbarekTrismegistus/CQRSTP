package ma.tpcqrs.bankaccount.command.events;

import java.math.BigDecimal;

public record AccountCreditedEvent(String accountId, BigDecimal amount, String currency) {
}

package ma.tpcqrs.bankaccount.command.events;

import java.math.BigDecimal;

public record AccountDebitedEvent(String accountId, BigDecimal amount, String currency) {
}

package ma.tpcqrs.bankaccount.command.aggregates;

import ma.tpcqrs.bankaccount.command.commands.AddAccountCommand;
import ma.tpcqrs.bankaccount.command.commands.CreditAccountCommand;
import ma.tpcqrs.bankaccount.command.commands.DebitAccountCommand;
import ma.tpcqrs.bankaccount.command.commands.UpdateAccountStatusCommand;
import ma.tpcqrs.bankaccount.command.events.AccountActivatedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountCreatedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountCreditedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountDebitedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountStatusUpdatedEvent;
import ma.tpcqrs.bankaccount.common.AccountStatus;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;

@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    private String accountId;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;

    protected AccountAggregate() {
        // Required by Axon when it rebuilds aggregates from the event stream.
    }

    @CommandHandler
    public AccountAggregate(AddAccountCommand command) {
        requirePositive(command.initialBalance(), "Initial balance must be positive");
        requireCurrency(command.currency());
        AggregateLifecycle.apply(new AccountCreatedEvent(
                command.accountId(), command.initialBalance(), normalizeCurrency(command.currency()), AccountStatus.CREATED));
        // The demonstration creates and activates a new account in one command.
        AggregateLifecycle.apply(new AccountActivatedEvent(command.accountId(), AccountStatus.ACTIVATED));
    }

    @CommandHandler
    public void handle(CreditAccountCommand command) {
        requireActivated();
        requirePositive(command.amount(), "Credit amount must be positive");
        requireMatchingCurrency(command.currency());
        AggregateLifecycle.apply(new AccountCreditedEvent(accountId, command.amount(), normalizeCurrency(command.currency())));
    }

    @CommandHandler
    public void handle(DebitAccountCommand command) {
        requireActivated();
        requirePositive(command.amount(), "Debit amount must be positive");
        requireMatchingCurrency(command.currency());
        if (balance.compareTo(command.amount()) < 0) {
            throw new IllegalArgumentException("Insufficient account balance");
        }
        AggregateLifecycle.apply(new AccountDebitedEvent(accountId, command.amount(), normalizeCurrency(command.currency())));
    }

    @CommandHandler
    public void handle(UpdateAccountStatusCommand command) {
        if (status == null) {
            throw new IllegalStateException("Account does not exist");
        }
        if (status == command.status()) {
            throw new IllegalArgumentException("Account is already in status " + status);
        }
        AggregateLifecycle.apply(new AccountStatusUpdatedEvent(accountId, command.status()));
    }

    @EventSourcingHandler
    public void on(AccountCreatedEvent event) {
        accountId = event.accountId();
        balance = event.initialBalance();
        currency = event.currency();
        status = event.status();
    }

    @EventSourcingHandler
    public void on(AccountActivatedEvent event) {
        status = event.status();
    }

    @EventSourcingHandler
    public void on(AccountCreditedEvent event) {
        balance = balance.add(event.amount());
    }

    @EventSourcingHandler
    public void on(AccountDebitedEvent event) {
        balance = balance.subtract(event.amount());
    }

    @EventSourcingHandler
    public void on(AccountStatusUpdatedEvent event) {
        status = event.status();
    }

    private void requireActivated() {
        if (status != AccountStatus.ACTIVATED) {
            throw new IllegalStateException("Account " + accountId + " is not activated");
        }
    }

    private void requireMatchingCurrency(String requestedCurrency) {
        requireCurrency(requestedCurrency);
        if (!currency.equalsIgnoreCase(requestedCurrency.trim())) {
            throw new IllegalArgumentException("Operation currency must match account currency " + currency);
        }
    }

    private static void requirePositive(BigDecimal amount, String message) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void requireCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }
    }

    private static String normalizeCurrency(String currency) {
        return currency.trim().toUpperCase();
    }
}

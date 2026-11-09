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
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class AccountAggregateTest {
    private static final String ACCOUNT_ID = "account-123";
    private AggregateTestFixture<AccountAggregate> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(AccountAggregate.class);
    }

    @Test
    void creatingAnAccountEmitsCreatedAndActivatedEvents() {
        fixture.givenNoPriorActivity()
                .when(new AddAccountCommand(ACCOUNT_ID, amount("1000"), "mad"))
                .expectEvents(
                        new AccountCreatedEvent(ACCOUNT_ID, amount("1000"), "MAD", AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED));
    }

    @Test
    void rejectsNonPositiveOpeningBalance() {
        fixture.givenNoPriorActivity()
                .when(new AddAccountCommand(ACCOUNT_ID, amount("0"), "MAD"))
                .expectException(IllegalArgumentException.class)
                .expectNoEvents();
    }

    @Test
    void creditAndDebitEmitEventsAndRespectBalance() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, amount("1000"), "MAD", AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new CreditAccountCommand(ACCOUNT_ID, amount("250"), "MAD"))
                .expectEvents(new AccountCreditedEvent(ACCOUNT_ID, amount("250"), "MAD"));

        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, amount("1000"), "MAD", AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new DebitAccountCommand(ACCOUNT_ID, amount("400"), "MAD"))
                .expectEvents(new AccountDebitedEvent(ACCOUNT_ID, amount("400"), "MAD"));
    }

    @Test
    void rejectsDebitAboveBalance() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, amount("100"), "MAD", AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new DebitAccountCommand(ACCOUNT_ID, amount("101"), "MAD"))
                .expectException(IllegalArgumentException.class)
                .expectNoEvents();
    }

    @Test
    void rejectsOperationsWhenAccountIsNotActivated() {
        fixture.given(new AccountCreatedEvent(ACCOUNT_ID, amount("100"), "MAD", AccountStatus.CREATED))
                .when(new CreditAccountCommand(ACCOUNT_ID, amount("10"), "MAD"))
                .expectException(IllegalStateException.class)
                .expectNoEvents();
    }

    @Test
    void updatesStatusAndRejectsDuplicateStatus() {
        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, amount("100"), "MAD", AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .when(new UpdateAccountStatusCommand(ACCOUNT_ID, AccountStatus.SUSPENDED))
                .expectEvents(new AccountStatusUpdatedEvent(ACCOUNT_ID, AccountStatus.SUSPENDED));

        fixture.given(
                        new AccountCreatedEvent(ACCOUNT_ID, amount("100"), "MAD", AccountStatus.CREATED),
                        new AccountActivatedEvent(ACCOUNT_ID, AccountStatus.ACTIVATED))
                .andGiven(new AccountStatusUpdatedEvent(ACCOUNT_ID, AccountStatus.SUSPENDED))
                .when(new UpdateAccountStatusCommand(ACCOUNT_ID, AccountStatus.SUSPENDED))
                .expectException(IllegalArgumentException.class)
                .expectNoEvents();
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}

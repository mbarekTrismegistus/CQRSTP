package ma.tpcqrs.bankaccount.query.handlers;

import ma.tpcqrs.bankaccount.command.events.AccountActivatedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountCreatedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountCreditedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountDebitedEvent;
import ma.tpcqrs.bankaccount.command.events.AccountStatusUpdatedEvent;
import ma.tpcqrs.bankaccount.common.OperationType;
import ma.tpcqrs.bankaccount.query.entities.Account;
import ma.tpcqrs.bankaccount.query.entities.AccountOperation;
import ma.tpcqrs.bankaccount.query.queries.WatchAccountOperationsQuery;
import ma.tpcqrs.bankaccount.query.repositories.AccountOperationRepository;
import ma.tpcqrs.bankaccount.query.repositories.AccountRepository;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@Transactional
public class AccountEventHandler {
    private final AccountRepository accountRepository;
    private final AccountOperationRepository operationRepository;
    private final QueryUpdateEmitter queryUpdateEmitter;

    public AccountEventHandler(
            AccountRepository accountRepository,
            AccountOperationRepository operationRepository,
            QueryUpdateEmitter queryUpdateEmitter
    ) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    public void on(AccountCreatedEvent event, @Timestamp Instant timestamp) {
        accountRepository.save(new Account(
                event.accountId(), event.initialBalance(), event.currency(), event.status(), timestamp));
    }

    @EventHandler
    public void on(AccountActivatedEvent event) {
        findAccount(event.accountId()).setStatus(event.status());
    }

    @EventHandler
    public void on(AccountStatusUpdatedEvent event) {
        findAccount(event.accountId()).setStatus(event.status());
    }

    @EventHandler
    public void on(AccountCreditedEvent event, @Timestamp Instant timestamp) {
        Account account = findAccount(event.accountId());
        account.setBalance(account.getBalance().add(event.amount()));
        saveAndEmitOperation(new AccountOperation(
                event.accountId(), event.amount(), event.currency(), OperationType.CREDIT, timestamp));
    }

    @EventHandler
    public void on(AccountDebitedEvent event, @Timestamp Instant timestamp) {
        Account account = findAccount(event.accountId());
        account.setBalance(account.getBalance().subtract(event.amount()));
        saveAndEmitOperation(new AccountOperation(
                event.accountId(), event.amount(), event.currency(), OperationType.DEBIT, timestamp));
    }

    private Account findAccount(String accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalStateException("Read model is missing account " + accountId));
    }

    private void saveAndEmitOperation(AccountOperation operation) {
        AccountOperation savedOperation = operationRepository.save(operation);
        queryUpdateEmitter.emit(
                WatchAccountOperationsQuery.class,
                query -> query.accountId().equals(savedOperation.getAccountId()),
                savedOperation);
    }
}

package ma.tpcqrs.bankaccount.query.handlers;

import ma.tpcqrs.bankaccount.query.dto.AccountStatement;
import ma.tpcqrs.bankaccount.query.entities.Account;
import ma.tpcqrs.bankaccount.query.entities.AccountOperation;
import ma.tpcqrs.bankaccount.query.queries.GetAccountStatementQuery;
import ma.tpcqrs.bankaccount.query.queries.GetAllAccountsQuery;
import ma.tpcqrs.bankaccount.query.queries.WatchAccountOperationsQuery;
import ma.tpcqrs.bankaccount.query.repositories.AccountOperationRepository;
import ma.tpcqrs.bankaccount.query.repositories.AccountRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountQueryHandler {
    private final AccountRepository accountRepository;
    private final AccountOperationRepository operationRepository;

    public AccountQueryHandler(AccountRepository accountRepository, AccountOperationRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
    }

    @QueryHandler
    public List<Account> handle(GetAllAccountsQuery query) {
        return accountRepository.findAll();
    }

    @QueryHandler
    public AccountStatement handle(GetAccountStatementQuery query) {
        Account account = accountRepository.findById(query.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + query.accountId()));
        List<AccountOperation> operations = operationRepository.findByAccountIdOrderByOperatedAtAsc(query.accountId());
        return new AccountStatement(account, operations);
    }

    @QueryHandler
    public List<AccountOperation> handle(WatchAccountOperationsQuery query) {
        return operationRepository.findByAccountIdOrderByOperatedAtAsc(query.accountId());
    }
}

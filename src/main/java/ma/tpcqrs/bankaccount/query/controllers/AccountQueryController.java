package ma.tpcqrs.bankaccount.query.controllers;

import ma.tpcqrs.bankaccount.query.dto.AccountStatement;
import ma.tpcqrs.bankaccount.query.entities.Account;
import ma.tpcqrs.bankaccount.query.entities.AccountOperation;
import ma.tpcqrs.bankaccount.query.queries.GetAccountStatementQuery;
import ma.tpcqrs.bankaccount.query.queries.GetAllAccountsQuery;
import ma.tpcqrs.bankaccount.query.queries.WatchAccountOperationsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/query/account")
public class AccountQueryController {
    private final QueryGateway queryGateway;

    public AccountQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/all")
    public CompletableFuture<List<Account>> getAllAccounts() {
        return queryGateway.query(
                new GetAllAccountsQuery(), ResponseTypes.multipleInstancesOf(Account.class));
    }

    @GetMapping("/{accountId}/statement")
    public CompletableFuture<AccountStatement> getAccountStatement(@PathVariable String accountId) {
        return queryGateway.query(
                new GetAccountStatementQuery(accountId), ResponseTypes.instanceOf(AccountStatement.class));
    }

    @GetMapping(value = "/{accountId}/watch", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AccountOperation> watchAccountOperations(@PathVariable String accountId) {
        SubscriptionQueryResult<List<AccountOperation>, AccountOperation> subscription = queryGateway.subscriptionQuery(
                new WatchAccountOperationsQuery(accountId),
                ResponseTypes.multipleInstancesOf(AccountOperation.class),
                ResponseTypes.instanceOf(AccountOperation.class));

        Flux<AccountOperation> initialOperations = subscription.initialResult().flatMapMany(Flux::fromIterable);
        return Flux.concat(initialOperations, subscription.updates())
                .doFinally(signal -> subscription.close());
    }
}

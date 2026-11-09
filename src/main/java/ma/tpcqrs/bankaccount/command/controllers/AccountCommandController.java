package ma.tpcqrs.bankaccount.command.controllers;

import jakarta.validation.Valid;
import ma.tpcqrs.bankaccount.command.commands.AddAccountCommand;
import ma.tpcqrs.bankaccount.command.commands.CreditAccountCommand;
import ma.tpcqrs.bankaccount.command.commands.DebitAccountCommand;
import ma.tpcqrs.bankaccount.command.commands.UpdateAccountStatusCommand;
import ma.tpcqrs.bankaccount.command.dto.AccountOperationRequest;
import ma.tpcqrs.bankaccount.command.dto.AddAccountRequest;
import ma.tpcqrs.bankaccount.command.dto.UpdateAccountStatusRequest;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/commands/account")
public class AccountCommandController {
    private final CommandGateway commandGateway;

    public AccountCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping("/add")
    public CompletableFuture<String> addAccount(@Valid @RequestBody AddAccountRequest request) {
        String accountId = UUID.randomUUID().toString();
        return commandGateway.<Object>send(new AddAccountCommand(
                        accountId, request.initialBalance(), request.currency()))
                .thenApply(ignored -> accountId);
    }

    @PostMapping("/{accountId}/credit")
    public CompletableFuture<Void> creditAccount(
            @PathVariable String accountId,
            @Valid @RequestBody AccountOperationRequest request
    ) {
        return commandGateway.send(new CreditAccountCommand(accountId, request.amount(), request.currency()));
    }

    @PostMapping("/{accountId}/debit")
    public CompletableFuture<Void> debitAccount(
            @PathVariable String accountId,
            @Valid @RequestBody AccountOperationRequest request
    ) {
        return commandGateway.send(new DebitAccountCommand(accountId, request.amount(), request.currency()));
    }

    @PutMapping("/{accountId}/status")
    public CompletableFuture<Void> updateStatus(
            @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountStatusRequest request
    ) {
        return commandGateway.send(new UpdateAccountStatusCommand(accountId, request.status()));
    }
}

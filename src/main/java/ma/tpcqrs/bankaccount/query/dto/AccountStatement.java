package ma.tpcqrs.bankaccount.query.dto;

import ma.tpcqrs.bankaccount.query.entities.Account;
import ma.tpcqrs.bankaccount.query.entities.AccountOperation;

import java.util.List;

public record AccountStatement(Account account, List<AccountOperation> operations) {
}

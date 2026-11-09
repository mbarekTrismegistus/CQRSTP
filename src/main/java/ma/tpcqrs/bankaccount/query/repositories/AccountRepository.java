package ma.tpcqrs.bankaccount.query.repositories;

import ma.tpcqrs.bankaccount.query.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}

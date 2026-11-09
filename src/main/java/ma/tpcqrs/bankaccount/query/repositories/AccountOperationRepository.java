package ma.tpcqrs.bankaccount.query.repositories;

import ma.tpcqrs.bankaccount.query.entities.AccountOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountOperationRepository extends JpaRepository<AccountOperation, Long> {
    List<AccountOperation> findByAccountIdOrderByOperatedAtAsc(String accountId);
}

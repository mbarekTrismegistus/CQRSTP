package ma.tpcqrs.bankaccount.query.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import ma.tpcqrs.bankaccount.common.OperationType;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_operations", indexes = @Index(name = "idx_operation_account_id", columnList = "account_id"))
public class AccountOperation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, length = 36)
    private String accountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, length = 10)
    private OperationType type;

    @Column(name = "operated_at", nullable = false)
    private Instant operatedAt;

    protected AccountOperation() {
    }

    public AccountOperation(String accountId, BigDecimal amount, String currency, OperationType type, Instant operatedAt) {
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.operatedAt = operatedAt;
    }

    public Long getId() { return id; }
    public String getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public OperationType getType() { return type; }
    public Instant getOperatedAt() { return operatedAt; }
}

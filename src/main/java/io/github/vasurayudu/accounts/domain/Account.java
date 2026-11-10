package io.github.vasurayudu.accounts.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private Instant createdAt;

    /** Optimistic locking prevents two concurrent transactions from overwriting each other's balance. */
    @Version
    private long version;

    protected Account() {
        // for JPA
    }

    public Account(String customerName, AccountType type) {
        this.id = UUID.randomUUID();
        this.customerName = customerName;
        this.type = type;
        this.balance = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    public void deposit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requirePositive(amount);
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(id, balance, amount);
        }
        balance = balance.subtract(amount);
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public UUID getId() { return id; }
    public String getCustomerName() { return customerName; }
    public AccountType getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public Instant getCreatedAt() { return createdAt; }
}

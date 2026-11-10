package io.github.vasurayudu.accounts.service;

import io.github.vasurayudu.accounts.domain.Account;
import io.github.vasurayudu.accounts.domain.AccountRepository;
import io.github.vasurayudu.accounts.domain.AccountType;
import io.github.vasurayudu.accounts.events.AccountEvent;
import io.github.vasurayudu.accounts.events.AccountEvent.EventType;
import io.github.vasurayudu.accounts.events.AccountEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository repository;
    private final AccountEventPublisher publisher;

    public AccountService(AccountRepository repository, AccountEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public Account open(String customerName, AccountType type) {
        Account account = repository.save(new Account(customerName, type));
        publisher.publish(AccountEvent.of(EventType.ACCOUNT_OPENED, account.getId(), BigDecimal.ZERO, account.getBalance()));
        return account;
    }

    @Transactional(readOnly = true)
    public Account get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public Account deposit(UUID id, BigDecimal amount) {
        Account account = get(id);
        account.deposit(amount);
        publisher.publish(AccountEvent.of(EventType.DEPOSIT, id, amount, account.getBalance()));
        return account;
    }

    @Transactional
    public Account withdraw(UUID id, BigDecimal amount) {
        Account account = get(id);
        account.withdraw(amount);
        publisher.publish(AccountEvent.of(EventType.WITHDRAWAL, id, amount, account.getBalance()));
        return account;
    }
}

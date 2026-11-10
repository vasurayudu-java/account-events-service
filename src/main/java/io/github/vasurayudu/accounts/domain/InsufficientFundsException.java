package io.github.vasurayudu.accounts.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(UUID accountId, BigDecimal balance, BigDecimal requested) {
        super("Account %s has balance %s, cannot withdraw %s".formatted(accountId, balance, requested));
    }
}

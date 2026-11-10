package io.github.vasurayudu.accounts.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published to Kafka whenever an account changes.
 * The eventId lets consumers detect and ignore duplicate deliveries (idempotency).
 */
public record AccountEvent(
        UUID eventId,
        EventType type,
        UUID accountId,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant occurredAt
) {
    public enum EventType { ACCOUNT_OPENED, DEPOSIT, WITHDRAWAL }

    public static AccountEvent of(EventType type, UUID accountId, BigDecimal amount, BigDecimal balanceAfter) {
        return new AccountEvent(UUID.randomUUID(), type, accountId, amount, balanceAfter, Instant.now());
    }
}

package io.github.vasurayudu.accounts.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example downstream consumer, for instance a notification or fraud-screening service.
 * Large withdrawals are flagged. Events that keep failing go to the dead-letter topic (see KafkaConfig).
 */
@Component
public class AccountEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);
    private static final BigDecimal LARGE_WITHDRAWAL = new BigDecimal("10000");

    /** In-memory idempotency store for the demo. A real service would use a database table or Redis. */
    private final Set<UUID> processedEventIds = ConcurrentHashMap.newKeySet();

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "notification-service")
    public void onEvent(AccountEvent event) {
        if (!processedEventIds.add(event.eventId())) {
            log.info("Skipping duplicate event {}", event.eventId());
            return;
        }
        if (event.amount() == null) {
            throw new IllegalStateException("Event " + event.eventId() + " has no amount");
        }
        if (event.type() == AccountEvent.EventType.WITHDRAWAL && event.amount().compareTo(LARGE_WITHDRAWAL) >= 0) {
            log.warn("ALERT: large withdrawal of {} on account {}", event.amount(), event.accountId());
        } else {
            log.info("Processed {} of {} on account {}", event.type(), event.amount(), event.accountId());
        }
    }
}

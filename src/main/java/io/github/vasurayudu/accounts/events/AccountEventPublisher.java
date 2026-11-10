package io.github.vasurayudu.accounts.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AccountEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountEventPublisher.class);

    private final KafkaTemplate<String, AccountEvent> kafkaTemplate;
    private final String topic;

    public AccountEventPublisher(KafkaTemplate<String, AccountEvent> kafkaTemplate,
                                 @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * The account id is the message key, so every event for one account goes to the same
     * partition and consumers see them in order.
     */
    public void publish(AccountEvent event) {
        kafkaTemplate.send(topic, event.accountId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event {} for account {}", event.eventId(), event.accountId(), ex);
                    } else {
                        log.info("Published {} for account {} to partition {} offset {}",
                                event.type(), event.accountId(),
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }
}

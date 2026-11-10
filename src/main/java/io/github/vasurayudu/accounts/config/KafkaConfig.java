package io.github.vasurayudu.accounts.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    NewTopic accountEventsTopic(@Value("${app.kafka.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(3).replicas(1).build();
    }

    /** Dead-letter topic: by default Spring Kafka sends failures to "<topic>-dlt" on the same partition. */
    @Bean
    NewTopic accountEventsDeadLetterTopic(@Value("${app.kafka.topic}") String topic) {
        return TopicBuilder.name(topic + "-dlt").partitions(3).replicas(1).build();
    }

    /**
     * Retries a failing message 3 times with exponential backoff (1s, 2s, 4s),
     * then publishes it to the dead-letter topic so the partition is not blocked.
     */
    @Bean
    DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> template) {
        var recoverer = new DeadLetterPublishingRecoverer(template);
        var backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxAttempts(3);
        var handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(IllegalArgumentException.class);
        handler.setRetryListeners((record, ex, attempt) ->
                log.warn("Retry {} for record key={} : {}", attempt, record.key(), ex.getMessage()));
        return handler;
    }
}

package com.sridevi.urlshortener.kafka.producer;

import com.sridevi.urlshortener.kafka.KafkaTopics;
import com.sridevi.urlshortener.kafka.event.UrlClickedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class KafkaAnalyticsEventProducer
        implements AnalyticsEventProducer {

    private final KafkaTemplate<String, UrlClickedEvent> kafkaTemplate;

    public KafkaAnalyticsEventProducer(
            KafkaTemplate<String, UrlClickedEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<Void> publish(
            UrlClickedEvent event
    ) {

        return kafkaTemplate
                .send(
                        KafkaTopics.CLICK_EVENTS,
                        event.shortCode(),
                        event
                )
                .thenApply(result -> null);
    }
}
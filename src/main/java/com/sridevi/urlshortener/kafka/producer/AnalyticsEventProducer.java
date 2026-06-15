package com.sridevi.urlshortener.kafka.producer;

import com.sridevi.urlshortener.kafka.event.UrlClickedEvent;
import java.util.concurrent.CompletableFuture;

/** Placeholder contract; no Kafka producer is wired yet. */
public interface AnalyticsEventProducer { CompletableFuture<Void> publish(UrlClickedEvent event); }

package com.sridevi.urlshortener.kafka.consumer;

import com.sridevi.urlshortener.kafka.event.UrlClickedEvent;

/** Placeholder contract; no Kafka listener is wired yet. */
public interface AnalyticsEventConsumer { void consume(UrlClickedEvent event); }

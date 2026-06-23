package com.sridevi.urlshortener.kafka.consumer;

import com.sridevi.urlshortener.kafka.KafkaTopics;
import com.sridevi.urlshortener.kafka.event.UrlClickedEvent;
import com.sridevi.urlshortener.service.AnalyticsService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaAnalyticsEventConsumer {

    private final AnalyticsService analyticsService;

    public KafkaAnalyticsEventConsumer(
            AnalyticsService analyticsService
    ) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(
            topics = KafkaTopics.CLICK_EVENTS,
            groupId = "analytics-group"
    )
    public void consume(
            UrlClickedEvent event
    ) {

    	System.out.println(
    	        "KAFKA EVENT RECEIVED => "
    	                + event.shortCode()
    	);
        analyticsService.recordClick(
                event.shortCode()
        );
    }
}
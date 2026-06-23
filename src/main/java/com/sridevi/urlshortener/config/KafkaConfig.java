package com.sridevi.urlshortener.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.sridevi.urlshortener.kafka.event.UrlClickedEvent;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic clickEventsTopic() {
        return TopicBuilder
                .name("click-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, UrlClickedEvent> producerFactory() {

        Map<String,Object> config = new HashMap<>();

        config.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        config.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        config.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class
        );

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, UrlClickedEvent> kafkaTemplate(
            ProducerFactory<String, UrlClickedEvent> factory
    ) {
        return new KafkaTemplate<>(factory);
    }
    
    @Bean
    public ConsumerFactory<String, UrlClickedEvent> consumerFactory() {

        JsonDeserializer<UrlClickedEvent> deserializer =
                new JsonDeserializer<>(UrlClickedEvent.class);

        deserializer.addTrustedPackages("*");

        Map<String,Object> props = new HashMap<>();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                "localhost:9092"
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "analytics-group"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

//        props.put(
//        	    JsonDeserializer.VALUE_DEFAULT_TYPE,
//        	    UrlClickedEvent.class.getName()
//        	);
//
//        	props.put(
//        	    JsonDeserializer.TRUSTED_PACKAGES,
//        	    "*"
//        	);
//
//        	props.put(
//        	    JsonDeserializer.USE_TYPE_INFO_HEADERS,
//        	    false
//        	);
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UrlClickedEvent>
    kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, UrlClickedEvent>
                factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory()
        );

        return factory;
    }
}
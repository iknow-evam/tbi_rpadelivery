package com.evam.marketing.communication.template.service.event;

//import com.evam.marketing.communication.template.configuration.kafka.property.KafkaProperties;
import com.evam.marketing.communication.template.service.event.model.CommunicationResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by cemserit on 2.03.2021.
 */
@Service
@Slf4j
public class KafkaProducerService {
/*
    private final KafkaTemplate<String, CommunicationResponseEvent> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final KafkaCommunicationResponseEventCallback eventCallback;

    public KafkaProducerService(KafkaTemplate<String, CommunicationResponseEvent> kafkaTemplate,
                                KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
        this.eventCallback = new KafkaCommunicationResponseEventCallback();
    }

    public void sendEvent(CommunicationResponseEvent event) {
        String eventName = kafkaProperties.getEventTopic().getName();
        ProducerRecord<String, CommunicationResponseEvent> record = new ProducerRecord<>
                (eventName, event.getActorId(), event);

        kafkaTemplate.send(record)
                .addCallback(eventCallback);
        log.debug("Kafka communication event successfully sent. {}", event);
    }

    public void sendEvents(List<CommunicationResponseEvent> events) {
        events.forEach(this::sendEvent);
    }

    */
}

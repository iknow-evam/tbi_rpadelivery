package com.evam.marketing.communication.template.service.event;

import com.evam.marketing.communication.template.service.event.model.CommunicationResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Created by cemserit on 2.03.2021.
 */
@Slf4j
public class KafkaCommunicationResponseEventCallback implements ListenableFutureCallback<SendResult<String,
        CommunicationResponseEvent>> {
    @Override
    public void onFailure(Throwable ex) {
        log.warn("Kafka message send fail!", ex);
    }

    @Override
    public void onSuccess(SendResult<String, CommunicationResponseEvent> result) {
        log.info("Kafka message successfully sent. {}", result);
    }
}

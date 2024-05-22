package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.model.response.SpeedyCardShipmentTracking;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.event.model.CommunicationResponseEvent;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import org.json.JSONObject;

public abstract class AbstractCommunicationClient implements CommunicationClient {
/*
    private final KafkaProducerService kafkaProducerService;

    protected AbstractCommunicationClient(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    protected CommunicationResponse generateSuccessCommunicationResponse(
            CommunicationRequest communicationRequest, SpeedyCardShipmentTracking speedyCardShipmentTracking, String phone_number) {
        return CommunicationResponse.builder()
                .success(true)
                .communicationCode(communicationRequest.getCommunicationCode())
                .communicationUUID(communicationRequest.getCommunicationUUID())
                .actorId(phone_number)
                .scenario(communicationRequest.getScenario())
                .cardId(speedyCardShipmentTracking.getCardId())
                .speedyDate(speedyCardShipmentTracking.getSpeedyDate())
                .note(speedyCardShipmentTracking.getNote())
                .deliveryStatus(speedyCardShipmentTracking.getDeliveryStatus())
                .rpaDate(speedyCardShipmentTracking.getRpaDate())
                .build();

    }
    protected CommunicationResponse generateFailCommunicationResponse(
            CommunicationRequest communicationRequest, String message, String reason) {
        return CommunicationResponse.builder()
                .success(false)
                .communicationCode(communicationRequest.getCommunicationCode())
                .communicationUUID(communicationRequest.getCommunicationUUID())
                .actorId(communicationRequest.getActorId())
                .scenario(communicationRequest.getScenario())
                .reason(reason)
                .build();
    }

    @Deprecated
    void sendFailEvent(CommunicationResponse communicationResponse) {
        CommunicationResponseEvent communicationResponseEvent = communicationResponse.toEvent();
        kafkaProducerService.sendEvent(communicationResponseEvent);
    }

    @Deprecated
    void sendSuccessEvent(CommunicationResponse communicationResponse) {
        CommunicationResponseEvent communicationResponseEvent = communicationResponse.toEvent();
        kafkaProducerService.sendEvent(communicationResponseEvent);
    }

    protected void sendEvent(CommunicationResponseEvent event) {
        kafkaProducerService.sendEvent(event);
    }
    */
}

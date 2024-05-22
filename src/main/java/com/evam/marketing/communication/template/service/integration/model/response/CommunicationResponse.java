package com.evam.marketing.communication.template.service.integration.model.response;

import com.evam.marketing.communication.template.repository.status.model.CustomCommunicationStatus;
import com.evam.marketing.communication.template.repository.status.model.StatusType;
import com.evam.marketing.communication.template.service.client.WorkerService;
import com.evam.marketing.communication.template.service.client.model.response.SpeedyCardShipmentTracking;
import com.evam.marketing.communication.template.service.event.model.CommunicationFailEvent;
import com.evam.marketing.communication.template.service.event.model.CommunicationResponseEvent;
import com.evam.marketing.communication.template.service.event.model.CommunicationSuccessEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONObject;

/**
 * Created by cemserit on 13.04.2021.
 */
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunicationResponse {
    WorkerService workerService;
    private boolean success;
    private String reason;
    @Setter
    private int cardId;
    @Setter
    private String speedyDate;
    @Setter
    private String note;
    @Setter
    private int deliveryStatus;
    @Setter
    private String rpaDate;

    @Setter
    private String provider;
    @Setter
    private String communicationUUID;
    @Setter
    private String communicationCode;
    @Setter
    private String scenario;
    @Setter
    private String actorId;
    @Setter
    private String notificationId;

    public CustomCommunicationStatus toCommunicationStatus() {
        String substringReason = Objects.isNull(reason) ? null :
                reason.substring(0, Math.min(reason.length(), 250));
        StatusType statusType = success ? StatusType.SENT : StatusType.FAILED;

        return CustomCommunicationStatus.builder()
                .communicationUUID(communicationUUID)
                .status(statusType.name())
                .provider(provider)
                .reason(substringReason)
                .statusUpdateTime(Timestamp.from(Instant.now()))
                .build();
    }

    public CommunicationResponseEvent toEvent() {
        return success ? toSuccessEvent() : toFailEvent();
    }

    private CommunicationResponseEvent toSuccessEvent() {
/*
        Map<String,Object> map = new HashMap<>();
        map.put("CardID",cardId);
        map.put("SpeedyDate",speedyDate);
        map.put("Note",note);
        map.put("DeliveryStatus",deliveryStatus);
        map.put("RpaDate",rpaDate);
        */
        return CommunicationSuccessEvent.builder()
                .communicationCode(communicationCode)
                .communicationUUID(communicationUUID)
                .scenario(scenario)
                .actorId(actorId)
                .cardId(getCardId())
                .deliveryStatus(getDeliveryStatus())
                .note(getNote())
                .speedyDate(getSpeedyDate())
                .rpaDate(getRpaDate())
                //.customParameters(map)
                //.message("CARD ID : "+map.get("CardID").toString())
                .build();
    }

    private CommunicationResponseEvent toFailEvent() {
        return CommunicationFailEvent.builder()
                .communicationCode(communicationCode)
                .communicationUUID(communicationUUID)
                .scenario(scenario)
                .actorId(actorId)
                .reason(reason)
                .build();
    }
}

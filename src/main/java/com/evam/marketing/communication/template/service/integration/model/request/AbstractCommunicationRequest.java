package com.evam.marketing.communication.template.service.integration.model.request;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Created by cemserit on 13.04.2021.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@SuperBuilder
public abstract class AbstractCommunicationRequest implements CommunicationRequest {
    private String messageType;
    private String actorId;
    private String communicationCode;
    private String communicationUUID;
    private String scenario;
    private int scenarioVersion;

    private int cardId;
    private int deliveryStatus;
    private String note;
    private String speedyDate;
    private String rpaDate;
    private String phoneNumber;

    public boolean isTransactional() {
        return "Transactional".equalsIgnoreCase(messageType);
    }
}

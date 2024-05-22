package com.evam.marketing.communication.template.service.event.model;

import lombok.Getter;

/**
 * Created by cemserit on 4.03.2021.
 */
@Getter
public enum CommunicationEventName {
    SUCCESS("rpaDeliveryStatus"), FAIL("communicationFail");

    private final String eventName;

    CommunicationEventName(String eventName) {
        this.eventName = eventName;
    }
}

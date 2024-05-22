package com.evam.marketing.communication.template.service.client.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;
@Data
@RequiredArgsConstructor
public class SpeedyCardShipmentTracking {
    private int id;
    private int cardId;
    private String shipmentId;
    private String speedyDate;
    private String note;
    private int deliveryStatus;
    private String parcelPlace;
    private String comment;
    private String rpaDate;
    private int exceptionStatus;
    private Object errorMessage;
    private Object errorDate;

}

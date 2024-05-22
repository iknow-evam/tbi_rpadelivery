package com.evam.marketing.communication.template.service.client.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;
@Data
@RequiredArgsConstructor
public class DeliveryAllStatus {
    private int id;
    private String processDate;
    private int status;
    private String fromFile;
    private String firstName;
    private String lastName;
    private String firstNameCyrillic;
    private String lastNameCyrillic;
    private String cardType;
    private String customerVCSNumber;
    private String streetType;
    private String street;
    private String streetNumber;
    private String apartmentNumber;
    private String zipCode;
    private String city;
    private String country;
    private String source;
    private String additionalData;
    private String additionalData2;
    private String phoneNumber;
    private String error;
    private String errorDate;
    private Object errorEmailDate;
    private Object errorFileName;
    private Object errorFileSentTo;
    private String speedyShipmentId;
    private String speedyPickupDate;
    private String speedyDeliveryDeadline;
    private double speedyCost;
    private double speedyCostVAT;
    private double speedyTotalCost;
    private int speedyParcelSeqNo;
    private String speedyParcelId;
    private Object speedyExternalCarrierId;
    private Object speedyExternalCarrierParcelNumber;
    private Object comitexFilename;
    private Object sentToComitexEmail;
    private Object sentToComitexDate;
    private java.util.ArrayList<SpeedyCardShipmentTracking> speedyCardShipmentTrackings;

    @Override
    public String toString() {
        return super.toString();
    }
}

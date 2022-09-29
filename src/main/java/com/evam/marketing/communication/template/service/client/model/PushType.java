package com.evam.marketing.communication.template.service.client.model;

public enum PushType {
    SIMPLE_PUSH("SIMPLE_PUSH"),
    LINK_PUSH("LINK_PUSH"),
    OFFER_PUSH("OFFER_PUSH"),
    BONUSOFFER_PUSH("BONUSOFFER_PUSH");

    private final String value;

    PushType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PushType fromString(String str) {
        for (PushType dataType : values()) {
            if (dataType.getValue().equalsIgnoreCase(str)) {
                return dataType;
            }
        }
        return null;
    }
}

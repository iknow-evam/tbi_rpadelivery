package com.evam.marketing.communication.template.service.client.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferPushRequest implements Serializable {
    public String msisdn;
    @JsonProperty("UACIInteractionPointName")
    public String uACIInteractionPointName;
    @JsonProperty("UACIInteractiveChannelname")
    public String uACIInteractiveChannelname;
    @JsonProperty("SessionID")
    public String sessionID;
    public String notificationTitle;
    public String notificationText;
    public String urlIdentifier;
    public String deepUrl;
    public String offerPayload;
}

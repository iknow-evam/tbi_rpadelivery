package com.evam.marketing.communication.template.service.client.model.request;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkPushRequest implements Serializable {
    public String msisdn;
    public String notificationTitle;
    public String notificationText;
    public String urlIdentifier;
    public String deepUrl;
}

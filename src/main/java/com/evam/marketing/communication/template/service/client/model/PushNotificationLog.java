package com.evam.marketing.communication.template.service.client.model;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Data
@Builder
@Entity
@Table(name = "LOG_PUSH_NOTIFICATION")
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @CreationTimestamp
    private LocalDateTime insertTime;

    private String campaignName;
    private String offerId;
    private String silentMode;
    private String msisdn;
    private String messageText;
    private String notificationType;
    private String status;
    private String fcmid;
    private String notificationTitle;
    private String notificationText;
    private String urlIdentifier;
    private String deepUrl;
    private String uaciInteractionPointName;
    private String uaciInteractiveChannelName;
    private String sessionId;
    private String commercialText;
    private String offerPayload;
    private String logger;
    private String endpoint;
    private String response;

    @Override
    public String toString() {
        return String.format("log push notification object is [campaignName = %s, offerId = %s, silentMode = %s, " +
                "msisdn = %s, messageText = %s, notificationType = %s, status = %s, fcmid = %s, " +
                "notificationTitle = %s, notificationText = %s, urlIdentifier = %s, deepUrl = %s, " +
                "uaciInteractionPointName = %s, uaciInteractiveChannelName = %s, sessionId = %s, " +
                "commercialText = %s, offerPayload = %s, logger = %s, endpoint = %s, response = %s].",
                campaignName, offerId, silentMode, msisdn, messageText, notificationType, status,
                fcmid, notificationTitle, notificationText, urlIdentifier, deepUrl, uaciInteractionPointName,
                uaciInteractiveChannelName, sessionId, commercialText, offerPayload, logger, endpoint, response);
    }
}

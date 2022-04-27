package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Helper {
    private static final String PUSH_NOTIFICATION = "https://apps.jazz.com.pk:8243/pushnotificationapi/1.0.0/pushnotification";
    private static final String PUSH_NOTIFICATION_LINK = "http://10.50.81.159:8280/jazzecare/1.0.0/pushnotificationforlink";
    private static final String PUSH_NOTIFICATION_OFFER = "http://10.50.81.159:8280/jazzecare/1.0.0/pushnotificationforrecommendedoffersforoffer";
    private static final String PUSH_NOTIFICATION_BONUS = "http://10.50.81.159:8280/jazzecare/1.0.0/pushnotificationforrecommendedoffersforbonusoffer";

    public static Map<String, String> convertToKeyValue(List<Parameter> params) {
        Map<String, String> keyValuePairs = new HashMap<>();
        for (int i = 0; i < params.size(); i++) {
            keyValuePairs.put(params.get(i).getName(), params.get(i).getValue());
        }
        return keyValuePairs;
    }

    public static String identifyEndpoint(Map<String, String> p) throws UnknownPayloadException {
        if (p.containsKey("msisdn")
                && p.containsKey("notificationType")
                && p.containsKey("status")
                && p.containsKey("fcmId")) {
            return PUSH_NOTIFICATION;
        } else if (p.containsKey("msisdn")
                && p.containsKey("notificationTitle")
                && p.containsKey("notificationText")
                && p.containsKey("urlIdentifier")
                && p.containsKey("deepUrl")) {
            return PUSH_NOTIFICATION_LINK;
        } else if (p.containsKey("msisdn")
                && p.containsKey("UACIInteractionPointName")
                && p.containsKey("UACIInteractiveChannelname")
                && p.containsKey("SessionID")
                && p.containsKey("notificationTitle")
                && p.containsKey("notificationText")
                && p.containsKey("urlIdentifier")
                && p.containsKey("deepUrl")
                && p.containsKey("offerPayload")) {
            return PUSH_NOTIFICATION_OFFER;
        } else if (p.containsKey("status")
                && p.containsKey("msisdn")
                && p.containsKey("notificationTitle")
                && p.containsKey("notificationText")
                && p.containsKey("commercialText")
                && p.containsKey("offerPayload")) {
            return PUSH_NOTIFICATION_BONUS;
        } else {
            throw new UnknownPayloadException("request parameters do not match any use case.");
        }
    }
}

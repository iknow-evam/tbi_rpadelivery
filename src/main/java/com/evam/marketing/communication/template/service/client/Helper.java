package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.Parameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

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

    public static String hit(String endpoint) throws Exception {
        // Create a neat value object to hold the URL
        //URL url = new URL("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY");
        //URL url = new URL("https://mocki.io/v1/188bf6aa-6b41-404b-be46-d8c947eb52ca");
        URL url = new URL(endpoint);

        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestMethod("POST");
        connection.setRequestProperty("accept", "application/json");
        connection.setRequestProperty("Authorization", "Bearer 4104f6e6-e091-3cc0-a163-41a9ebecac16");
        connection.setRequestProperty("content-type", "application/json");

        // This line makes the request
        InputStream responseStream = connection.getInputStream();
        return Helper.readData(responseStream);
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

    public static boolean isBetween9AmAnd9Pm() {
        Calendar am = Calendar.getInstance();
        am.set(Calendar.HOUR_OF_DAY, 8);
        am.set(Calendar.MINUTE, 59);
        am.set(Calendar.SECOND, 59);
        Calendar pm = Calendar.getInstance();
        pm.set(Calendar.HOUR_OF_DAY, 21);
        pm.set(Calendar.MINUTE, 0);
        pm.set(Calendar.SECOND, 0);
        Date now = new Date();

        return now.after(am.getTime()) && now.before(pm.getTime());
    }

    private static String readData(InputStream inputStream) {
        int bufferSize = 1024;
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try {
            for (int numRead; (numRead = in.read(buffer, 0, buffer.length)) > 0; ) {
                out.append(buffer, 0, numRead);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return out.toString();
    }
}

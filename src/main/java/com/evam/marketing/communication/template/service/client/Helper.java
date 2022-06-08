package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class Helper {
    public static Map<String, String> convertToKeyValue(List<Parameter> params) {
        Map<String, String> keyValuePairs = new HashMap<>();
        for (Parameter param : params) {
            keyValuePairs.put(param.getName(), param.getValue());
        }
        return keyValuePairs;
    }

    public static String hit(String endpoint) throws Exception {
        // Create a neat value object to hold the URL
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

    public static String identifyEndpoint(AppConfig appConfig, Map<String, String> p) throws UnknownPayloadException {
        log.info("identifying use case for the payload ({})", p);
        if (p.containsKey("MSISDN")
                && p.containsKey("NOTIFICATIONTYPE")
                && p.containsKey("STATUS")
                && p.containsKey("FCMID")) {
            log.info("use case push notification identified");
            return appConfig.notifyUrl;
        } else if (p.containsKey("MSISDN")
                && p.containsKey("NOTIFICATIONTITLE")
                && p.containsKey("NOTIFICATIONTEXT")
                && p.containsKey("URLIDENTIFIER")
                && p.containsKey("DEEPURL")) {
            log.info("use case push notification link identified");
            return appConfig.notifyLinkUrl;
        } else if (p.containsKey("MSISDN")
                && p.containsKey("INTERACTIONPOINTNAME")
                && p.containsKey("INTERACTIVECHANNELNAME")
                && p.containsKey("SESSIONID")
                && p.containsKey("NOTIFICATIONTITLE")
                && p.containsKey("NOTIFICATIONTEXT")
                && p.containsKey("URLIDENTIFIER")
                && p.containsKey("DEEPURL")
                && p.containsKey("OFFERPAYLOAD")) {
            log.info("use case push notification offer identified");
            return appConfig.notifyOfferUrl;
        } else if (p.containsKey("STATUS")
                && p.containsKey("MSISDN")
                && p.containsKey("NOTIFICATIONTITLE")
                && p.containsKey("NOTIFICATIONTEXT")
                && p.containsKey("COMMERCIALTEXT")
                && p.containsKey("OFFERPAYLOAD")) {
            log.info("use case push notification bonus identified");
            return appConfig.notifyBonusUrl;
        } else {
            log.info("throwing unknown payload exception");
            throw new UnknownPayloadException("request parameters do not match any use case.");
        }
    }

    public static boolean isTimeBetween(String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        LocalTime now = LocalTime.now();

        return start.isBefore(now) && end.isAfter(now);
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

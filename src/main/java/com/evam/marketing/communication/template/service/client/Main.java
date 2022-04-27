package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);
    private static final String URL_TOKEN = "http://10.50.13.117:8282/token";
    private static final String URL_MSG_SERVICE = "http://10.50.13.117:8282/whatsapp_generic/messageservice";

    public static void main(String[] args) throws Throwable {
        new Main().send();
    }

    public void send() {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter("msisdn", "923079771022"));
        params.add(new Parameter("notificationType", "subvasoffer"));
        params.add(new Parameter("status", "0"));
        params.add(new Parameter("fcmId", ""));

        try {
            Map<String, String> parameters = Helper.convertToKeyValue(params);
            String endpoint = Helper.identifyEndpoint(parameters);
            log.info("{} - {}", parameters, endpoint);

            this.call(endpoint);
        } catch (UnknownPayloadException ex) {
            log.error(ex);
        } catch (Throwable throwable) {
            log.error(throwable);
        }
    }

    public void call(String endpoint) throws Throwable {
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
        String result = this.readData(responseStream);

        // Manually converting the response body InputStream to APOD using Jackson
        //ObjectMapper mapper = new ObjectMapper();
        //ApacheOctetStreamData apod = mapper.readValue(responseStream, ApacheOctetStreamData.class);

        // Finally we have the response
        log.info(result);
    }

    private String readData(InputStream inputStream) {
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

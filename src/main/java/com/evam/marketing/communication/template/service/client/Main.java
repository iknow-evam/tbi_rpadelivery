package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
public class Main {

    private AppConfig appConfig = new AppConfig();

    public static void main(String[] args) throws Throwable {
        Main main = new Main();
        main.initConfig();
        main.send();
    }

    public void send() {
        List<Parameter> params = new ArrayList<>();
        params.add(new Parameter("MSISDN", "923079771022"));
        params.add(new Parameter("NOTIFICATIONTYPE", "subvasoffer"));
        params.add(new Parameter("STATUS", "0"));
        params.add(new Parameter("FCMID", ""));
        params.add(new Parameter("FCMID1", ""));

        try {
            Map<String, String> parameters = Helper.convertToKeyValue(params);
            String endpoint = Helper.identifyEndpoint(this.appConfig, parameters);
            log.info("payload is ({}) - endpoint({}) - msisdn({})", parameters, endpoint, parameters.get("MSISDN"));

            //this.call(endpoint);
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

    private void initConfig() {
        this.appConfig.notifyUrl = "https://apps.jazz.com.pk:8243/pushnotificationapi/1.0.0/pushnotification";
        this.appConfig.notifyLinkUrl = "http://10.50.81.159:8280/jazzecare/1.0.0/pushnotificationforlink";
        this.appConfig.notifyOfferUrl = "http://10.50.81.159:8280/jazzecare/1.0.0/pushnotificationforrecommendedoffersforoffer";
        this.appConfig.notifyBonusUrl = "http://10.50.81.159:8280/jazzecare/1.0.0/pushnotificationforrecommendedoffersforbonusoffer";
        this.appConfig.silentModeStart = "00:00:00";
        this.appConfig.silentModeEnd = "23:59:59";
    }
}

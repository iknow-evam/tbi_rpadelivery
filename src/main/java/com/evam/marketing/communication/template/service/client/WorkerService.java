package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.configuration.AppConfig;
import com.evam.marketing.communication.template.configuration.WebClientConfig;
import com.evam.marketing.communication.template.controller.CustomController.request.CardDeliveryRequest;
import com.evam.marketing.communication.template.service.client.model.CustomCommunicationRequest;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import com.evam.marketing.communication.template.service.client.model.response.DeliveryAllStatus;
import com.evam.marketing.communication.template.service.client.model.response.ErrorResponse;
import com.evam.marketing.communication.template.service.client.model.response.SpeedyCardShipmentTracking;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.json.JSONObject;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.postgresql.ds.PGPoolingDataSource;
import org.postgresql.ds.PGPoolingDataSource;

import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkerService {
    List<Object[]> batchParams = new ArrayList<>();
    List<Object[]> batchParams2 = new ArrayList<>();
    JSONObject jsonObject = new JSONObject();
    private final WebClientConfig webClientConfig;
    private static final String PARAMETER_KEY = "PARAMETER_KEY";
    private static final String PARAMETER_VALUE = "PARAMETER_VALUE";

    private final ObjectMapper objectMapper;
    public int countNoRecords = 0;
    Gson gson = new Gson();

    private final AppConfig appConfig;


    public WorkerService(WebClientConfig webClientConfig,
                         ObjectMapper objectMapper, AppConfig appConfig) {
        this.webClientConfig = webClientConfig;
        this.objectMapper = objectMapper;
        this.appConfig = appConfig;
    }

    public final PGPoolingDataSource createDataSource(String host, String db, String user, String password) {
        PGPoolingDataSource dataSource = new PGPoolingDataSource();
        dataSource.setServerName(host);
        dataSource.setPortNumber(5432);
        dataSource.setDatabaseName(db);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setMaxConnections(10);
        return dataSource;
    }

    @RateLimiter(name = "client-limiter")
    @NotNull
    public void callService(CardDeliveryRequest cardDeliveryRequest) {

        log.info("Card Delivery Request : {}", cardDeliveryRequest);

        String campaignName = cardDeliveryRequest.getCampaignName();
        String actorId = cardDeliveryRequest.getActorId();
        String key = cardDeliveryRequest.getKey();
        String value = cardDeliveryRequest.getValue();
        String key2 = "endDate";
        String value2 = "";


        try {
            if ((key == null && value == null) || (key.isEmpty() && value.isEmpty())) {
                log.info("Parameter value and key are null.");
            } else if (value.equalsIgnoreCase("ALL")) {
                JSONObject requestBody = new JSONObject();
                requestBody.put(key, "null");
                StringBuilder responseData = new StringBuilder();
                HttpPost httpPost = webClientConfig.httpClient();
                httpPost.setEntity(new StringEntity(requestBody.toString()));

                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(Integer.parseInt(appConfig.getTIMEOUT_MILLIS()))
                        .build();
                CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

                log.info("SERVICE STARTED..");

                Duration timeElapsed = null;
                Instant startService = Instant.now();

                CloseableHttpResponse httpResponse = httpclient.execute(httpPost);
                HttpEntity entity = httpResponse.getEntity();

                Instant startParsing = Instant.now();
                if (entity != null) {
                    log.info("Enter in entity condition.");
                    try (InputStream inputStream = entity.getContent();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), 1024 * 1024)) {
                        char[] buffer = new char[1024 * 1024];
                        int bytesRead;
                        while ((bytesRead = reader.read(buffer)) != -1) {
                            responseData.append(buffer, 0, bytesRead);
                        }
                    }
                }
                Instant stopParsing = Instant.now();
                timeElapsed = Duration.between(startParsing, stopParsing);
                log.info("TBIDeliveryAllCardStatus : Parsing Data Time Length {} ", timeElapsed.toMillis());

                String statusCode = String.valueOf(httpResponse.getStatusLine().getStatusCode());

                if (statusCode.equals("200")) {
                    DeliveryAllStatus[] deliveryAllStatuses = gson.fromJson(responseData.toString(), DeliveryAllStatus[].class);
                    log.info("DeliveryAllStatus Lenght {}", deliveryAllStatuses.length);
                    Arrays.stream(deliveryAllStatuses).forEach(arrayvalue -> {
                        if (!arrayvalue.getSpeedyCardShipmentTrackings().isEmpty()) {
                            if (arrayvalue.getSpeedyCardShipmentTrackings().size() > 0) {
                                SpeedyCardShipmentTracking speedyCardShipmentTracking = arrayvalue.getSpeedyCardShipmentTrackings()
                                        .get(arrayvalue.getSpeedyCardShipmentTrackings().size() - 1);

                                jsonObject.put("PHONE_NUMBER", arrayvalue.getPhoneNumber());
                                jsonObject.put("CARD_ID", speedyCardShipmentTracking.getCardId());
                                jsonObject.put("DELIVERY_STATUS", speedyCardShipmentTracking.getDeliveryStatus());
                                jsonObject.put("SPEEDY_DATE", speedyCardShipmentTracking.getSpeedyDate());
                                jsonObject.put("NOTE", speedyCardShipmentTracking.getNote());
                                jsonObject.put("RPA_DATE", speedyCardShipmentTracking.getRpaDate());

                                // Creating a map for quick lookup and replacement
                                Map<String, Object[]> batchParamsMap = batchParams.stream()
                                        .collect(Collectors.toMap(
                                                param -> (String) param[0],
                                                param -> param,
                                                (existing, replacement) -> existing));
                                // Checking if the phone number already exists in the map
                                if (batchParamsMap.containsKey(arrayvalue.getPhoneNumber())) {
                                    Object[] existingParams = batchParamsMap.get(arrayvalue.getPhoneNumber());
                                    String existingRpaDate = (String) existingParams[5];
                                    // Replacing the entry if the new RPA date is more recent
                                    if (speedyCardShipmentTracking.getRpaDate().compareTo(existingRpaDate) > 0) {
                                        batchParamsMap.put(arrayvalue.getPhoneNumber(), new Object[]{
                                                arrayvalue.getPhoneNumber(), speedyCardShipmentTracking.getCardId(), speedyCardShipmentTracking.getDeliveryStatus(), speedyCardShipmentTracking.getSpeedyDate(), speedyCardShipmentTracking.getNote(), speedyCardShipmentTracking.getRpaDate()
                                        });
                                    }
                                } else {
                                    // Adding the new entry if the phone number doesn't exist in the map
                                    batchParamsMap.put(arrayvalue.getPhoneNumber(), new Object[]{
                                            arrayvalue.getPhoneNumber(), speedyCardShipmentTracking.getCardId(), speedyCardShipmentTracking.getDeliveryStatus(), speedyCardShipmentTracking.getSpeedyDate(), speedyCardShipmentTracking.getNote(), speedyCardShipmentTracking.getRpaDate()
                                    });
                                }
                                // Updating the batchParams list with the new map values
                                batchParams = new ArrayList<>(batchParamsMap.values());
                            }
                        } else {
                            countNoRecords = countNoRecords + 1;
                        }
                    });
                    log.info("Null Records: {}", countNoRecords);
                } else {
                    log.info("STATUS CODE IS NOT 200");
                }

                PGPoolingDataSource dataSource = createDataSource(appConfig.getHOST(), appConfig.getDATABASE(), appConfig.getDB_USERNAME(), appConfig.getDB_PASSWORD());
                dataSource.setMaxConnections(20);
                try (Connection connection = dataSource.getConnection()) {
                    connection.setAutoCommit(false);
                    String insertSql = "INSERT INTO " + appConfig.getDB_TABLE_NAME() + " (phone_number, cardid, delivery_status, speedy_date, note, rpa_date, insert_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    String checkIfExistsSql = "SELECT count(*) FROM " + appConfig.getDB_TABLE_NAME() + " WHERE phone_number = ?";
                    String updateSql = "UPDATE " + appConfig.getDB_TABLE_NAME() + " SET cardid = ?, delivery_status = ?, speedy_date = ?, note = ?, rpa_date = ?, insert_date = ? WHERE phone_number = ?";
                    try (PreparedStatement pstmtCheckIfExists = connection.prepareStatement(checkIfExistsSql);
                         PreparedStatement pstmtUpdate = connection.prepareStatement(updateSql);
                         PreparedStatement pstmtInsert = connection.prepareStatement(insertSql)) {
                        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
                        for (Object[] params : batchParams2) {
                            pstmtCheckIfExists.setString(1, (String) params[0]);
                            try (ResultSet rs = pstmtCheckIfExists.executeQuery()) {
                                if (rs.next()) {
                                    int count = rs.getInt(1);
                                    if (count > 0) {
                                        // Kayıt mevcut, güncelle
                                        pstmtUpdate.setInt(1, (int) params[1]);
                                        pstmtUpdate.setInt(2, (int) params[2]);
                                        pstmtUpdate.setString(3, (String) params[3]);
                                        pstmtUpdate.setString(4, (String) params[4]);
                                        pstmtUpdate.setString(5, (String) params[5]);
                                        pstmtUpdate.setTimestamp(6, timestamp);
                                        pstmtUpdate.setString(7, (String) params[0]);
                                        pstmtUpdate.addBatch();
                                    } else {
                                        // Kayıt mevcut değil, ekle
                                        pstmtInsert.setString(1, (String) params[0]);
                                        pstmtInsert.setInt(2, (int) params[1]);
                                        pstmtInsert.setInt(3, (int) params[2]);
                                        pstmtInsert.setString(4, (String) params[3]);
                                        pstmtInsert.setString(5, (String) params[4]);
                                        pstmtInsert.setString(6, (String) params[5]);
                                        pstmtInsert.setTimestamp(7, timestamp);
                                        pstmtInsert.addBatch();
                                    }
                                }
                            }
                        }
                        pstmtInsert.executeBatch();
                        pstmtUpdate.executeBatch();

                        connection.commit(); // İşlemi tamamla
                        connection.close();
                        log.debug("Bulk insert executed successfully.");


                    }
                } catch (SQLException e) {
                    log.error("Error executing bulk update and insert: {}", e.getMessage());
                }

                Instant stopService = Instant.now();
                timeElapsed = Duration.between(startService, stopService);
                log.info("TBIDeliveryAllCardStatus : Service Time Length {} ", timeElapsed.toMillis());
            } else {
                log.info("SERVICE STARTED..");

                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectionRequestTimeout(Integer.parseInt(appConfig.getTIMEOUT_MILLIS()))
                        .build();
                CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
                StringBuilder responseData = new StringBuilder();
                HttpPost httpPost = webClientConfig.httpClient();
                JSONObject requestBody = new JSONObject();


                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                LocalDate startDate = LocalDate.parse(value, formatter);
                LocalDate currentDate = LocalDate.now();
                while (!startDate.isAfter(currentDate)) {
                    requestBody.put(key, startDate.format(formatter));
                    startDate = startDate.plusMonths(1);
                    if (startDate.isAfter(currentDate)) {
                        requestBody.put(key2, currentDate.format(formatter));
                    } else {
                        requestBody.put(key2, startDate.format(formatter));
                    }

                    httpPost.setEntity(new StringEntity(requestBody.toString(), "UTF-8"));
                    log.info("PARAMETER : {}", key2);

                    Duration timeElapsed = null;
                    Instant startService = Instant.now();

                    CloseableHttpResponse httpResponse = httpclient.execute(httpPost);
                    HttpEntity entity = httpResponse.getEntity();
                    Instant startParsing = Instant.now();
                    if (entity != null) {
                        log.info("Enter in entity condition.");
                        try (InputStream inputStream = entity.getContent();
                             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), 1024 * 1024)) {
                            char[] buffer = new char[1024 * 1024];
                            int bytesRead;
                            while ((bytesRead = reader.read(buffer)) != -1) {
                                responseData.append(buffer, 0, bytesRead);
                            }
                        }
                    }
                    Instant stopParsing = Instant.now();
                    timeElapsed = Duration.between(startParsing, stopParsing);
                    log.info("TBIDeliveryAllCardStatus : Parsing Data Time Length {} ", timeElapsed.toMillis());

                    log.info("Stream Closed.");
                    String statusCode = String.valueOf(httpResponse.getStatusLine().getStatusCode());
                    List<Object[]> batchParams = new ArrayList<>();

                    if (statusCode.equals("200")) {
                        DeliveryAllStatus[] deliveryAllStatuses = gson.fromJson(responseData.toString(), DeliveryAllStatus[].class);
                        log.info("DeliveryAllStatus Lenght {}", deliveryAllStatuses.length);
                        Arrays.stream(deliveryAllStatuses).forEach(arrayvalue -> {
                            if (!arrayvalue.getSpeedyCardShipmentTrackings().isEmpty()) {
                                if (arrayvalue.getSpeedyCardShipmentTrackings().size() > 0) {
                                    SpeedyCardShipmentTracking speedyCardShipmentTracking = arrayvalue.getSpeedyCardShipmentTrackings()
                                            .get(arrayvalue.getSpeedyCardShipmentTrackings().size() - 1);

                                    jsonObject.put("PHONE_NUMBER", arrayvalue.getPhoneNumber());
                                    jsonObject.put("CARD_ID", speedyCardShipmentTracking.getCardId());
                                    jsonObject.put("DELIVERY_STATUS", speedyCardShipmentTracking.getDeliveryStatus());
                                    jsonObject.put("SPEEDY_DATE", speedyCardShipmentTracking.getSpeedyDate());
                                    jsonObject.put("NOTE", speedyCardShipmentTracking.getNote());
                                    jsonObject.put("RPA_DATE", speedyCardShipmentTracking.getRpaDate());

                                    // Creating a map for quick lookup and replacement
                                    Map<String, Object[]> batchParamsMap = batchParams2.stream()
                                            .collect(Collectors.toMap(
                                                    param -> (String) param[0],
                                                    param -> param,
                                                    (existing, replacement) -> existing));
                                    // Checking if the phone number already exists in the map
                                    if (batchParamsMap.containsKey(arrayvalue.getPhoneNumber())) {
                                        Object[] existingParams = batchParamsMap.get(arrayvalue.getPhoneNumber());
                                        String existingRpaDate = (String) existingParams[5];
                                        // Replacing the entry if the new RPA date is more recent
                                        if (speedyCardShipmentTracking.getRpaDate().compareTo(existingRpaDate) > 0) {
                                            batchParamsMap.put(arrayvalue.getPhoneNumber(), new Object[]{
                                                    arrayvalue.getPhoneNumber(), speedyCardShipmentTracking.getCardId(), speedyCardShipmentTracking.getDeliveryStatus(), speedyCardShipmentTracking.getSpeedyDate(), speedyCardShipmentTracking.getNote(), speedyCardShipmentTracking.getRpaDate()
                                            });
                                        }
                                    } else {
                                        // Adding the new entry if the phone number doesn't exist in the map
                                        batchParamsMap.put(arrayvalue.getPhoneNumber(), new Object[]{
                                                arrayvalue.getPhoneNumber(), speedyCardShipmentTracking.getCardId(), speedyCardShipmentTracking.getDeliveryStatus(), speedyCardShipmentTracking.getSpeedyDate(), speedyCardShipmentTracking.getNote(), speedyCardShipmentTracking.getRpaDate()
                                        });
                                    }
                                    // Updating the batchParams list with the new map values
                                    batchParams2 = new ArrayList<>(batchParamsMap.values());
                                }
                            } else {
                                countNoRecords = countNoRecords + 1;
                            }
                        });
                        log.info("Null Records: {}", countNoRecords);

                    } else {
                        log.info("STATUS CODE IS NOT 200");
                    }
                    Instant stopService = Instant.now();
                    timeElapsed = Duration.between(startService, stopService);
                    log.info("TBIDeliveryAllCardStatus : Service Time Length {} ", timeElapsed.toMillis());

                    responseData.setLength(0);


                }
                PGPoolingDataSource dataSource = createDataSource(appConfig.getHOST(), appConfig.getDATABASE(), appConfig.getDB_USERNAME(), appConfig.getDB_PASSWORD());
                dataSource.setMaxConnections(20);
                try (Connection connection = dataSource.getConnection()) {
                    connection.setAutoCommit(false);
                    String insertSql = "INSERT INTO " + appConfig.getDB_TABLE_NAME() + " (phone_number, cardid, delivery_status, speedy_date, note, rpa_date, insert_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
                    String checkIfExistsSql = "SELECT count(*) FROM " + appConfig.getDB_TABLE_NAME() + " WHERE phone_number = ?";
                    String updateSql = "UPDATE " + appConfig.getDB_TABLE_NAME() + " SET cardid = ?, delivery_status = ?, speedy_date = ?, note = ?, rpa_date = ?, insert_date = ? WHERE phone_number = ?";
                    try (PreparedStatement pstmtCheckIfExists = connection.prepareStatement(checkIfExistsSql);
                         PreparedStatement pstmtUpdate = connection.prepareStatement(updateSql);
                         PreparedStatement pstmtInsert = connection.prepareStatement(insertSql)) {
                        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());
                        for (Object[] params : batchParams2) {
                            pstmtCheckIfExists.setString(1, (String) params[0]);
                            try (ResultSet rs = pstmtCheckIfExists.executeQuery()) {
                                if (rs.next()) {
                                    int count = rs.getInt(1);
                                    if (count > 0) {
                                        // Kayıt mevcut, güncelle
                                        pstmtUpdate.setInt(1, (int) params[1]);
                                        pstmtUpdate.setInt(2, (int) params[2]);
                                        pstmtUpdate.setString(3, (String) params[3]);
                                        pstmtUpdate.setString(4, (String) params[4]);
                                        pstmtUpdate.setString(5, (String) params[5]);
                                        pstmtUpdate.setTimestamp(6, timestamp);
                                        pstmtUpdate.setString(7, (String) params[0]);
                                        pstmtUpdate.addBatch();
                                    } else {
                                        // Kayıt mevcut değil, ekle
                                        pstmtInsert.setString(1, (String) params[0]);
                                        pstmtInsert.setInt(2, (int) params[1]);
                                        pstmtInsert.setInt(3, (int) params[2]);
                                        pstmtInsert.setString(4, (String) params[3]);
                                        pstmtInsert.setString(5, (String) params[4]);
                                        pstmtInsert.setString(6, (String) params[5]);
                                        pstmtInsert.setTimestamp(7, timestamp);
                                        pstmtInsert.addBatch();
                                    }
                                }
                            }
                        }
                        pstmtInsert.executeBatch();
                        pstmtUpdate.executeBatch();

                        connection.commit(); // İşlemi tamamla
                        connection.close();
                        log.debug("Bulk insert executed successfully.");

                    }
                } catch (SQLException e) {
                    log.error("Error executing bulk update and insert: {}", e.getMessage());
                }

            }
        } catch (Exception e) {
            log.error("TBICardStatus : Error {} ", e.toString());
        }
    }

    public static Map<String, String> convertToKeyValue(List<Parameter> params) {
        Map<String, String> keyValuePairs = new HashMap<>();
        for (Parameter param : params) {
            keyValuePairs.put(param.getName(), param.getValue());
        }
        return keyValuePairs;
    }

    private static Mono<? extends Throwable> handleClientError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.info("Error Body: " + body);
                    return Mono.error(new ErrorResponse(body, String.valueOf(response.statusCode().value())));
                });
    }


}

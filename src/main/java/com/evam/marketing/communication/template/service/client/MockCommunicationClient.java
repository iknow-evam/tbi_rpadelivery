package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.configuration.AppConfig;
import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.CustomCommunicationRequest;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;
import com.evam.marketing.communication.template.service.client.model.ServiceResponse;
import com.evam.marketing.communication.template.service.client.model.response.PushServiceResponse;
import com.evam.marketing.communication.template.service.client.repository.LogRepository;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import java.time.LocalDateTime;
import java.util.*;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Created by cemserit on 12.03.2021.
 */
@Service
@Slf4j
public class MockCommunicationClient extends AbstractCommunicationClient {
    private static final String PROVIDER = "MOCK_PROVIDER";
    private static final Random RANDOM = new Random();
    private static final String Y = "Y";
    private static final String N = "N";
    @Autowired
    private AppConfig appConfig;

    @Autowired
    private LogRepository logRepository;

    private final Helper helper;

    @Autowired
    public MockCommunicationClient(KafkaProducerService kafkaProducerService,Helper helper) {
        super(kafkaProducerService);
        this.helper = helper;
    }

    @Override
    @RateLimiter(name = "client-limiter")
    @NotNull
    public CommunicationResponse send(CommunicationRequest communicationRequest) {
        LocalDateTime now = LocalDateTime.now();

        CustomCommunicationRequest customCommunicationRequest = (CustomCommunicationRequest) communicationRequest;

        String campaignName = customCommunicationRequest.getScenario();
        String offerId = customCommunicationRequest.getCommunicationCode();
        log.debug("campaign name is {} and offer id is {}", campaignName, offerId);

        List<Parameter> parameters = customCommunicationRequest.getParameters();
        log.debug("parameters received are {}", parameters);
        Map<String, String> keyValueParam = helper.convertToKeyValue(parameters);
        String silentMode = keyValueParam.get("SILENTMODE");
        try {

            ServiceResponse response = new ServiceResponse();
            response.setResponse(new PushServiceResponse());
            if (silentMode.equalsIgnoreCase(N)) {
                if (helper.isTimeBetween(this.appConfig.silentModeStart, this.appConfig.silentModeEnd)) {
                    response = helper.webServiceCall(this.appConfig, keyValueParam);
                    if(response == null){
                        response.setResponse(PushServiceResponse.builder().msg("There is no response from server").build());
                    }
                    log.debug("response after hitting endpoint is {} ", response != null ? response : "no response");
                } else {
                    response.setResponse(PushServiceResponse.builder().msg("TIME CONSTRAINT").build());
                    silentMode = Y;
                    log.debug(String.valueOf(response));
                }
            } else {
                silentMode = Y;
            }
            PushNotificationLog pushLog = new PushNotificationLog();
            pushLog.campaignName(campaignName).silentMode(silentMode)
                    .msisdn(keyValueParam.get("MSISDN")).endpointType(keyValueParam.get("ENDPOINT_TYPE")).communicationCode(offerId).
                    response(silentMode.equalsIgnoreCase(Y) ? "Silent Mode" : (response.getResponse().msg != null ? response.getResponse().msg : "No response"))
                    .communicationUUID(communicationRequest.getCommunicationUUID());
            this.logRepository.save(pushLog);

            CommunicationResponse communicationSuccessResponse = generateSuccessCommunicationResponse(
                    communicationRequest, "Push Response", "Invoking push service is completed successfully");
            sendEvent(communicationSuccessResponse.toEvent());
            return communicationSuccessResponse;

        } catch (UnknownPayloadException ex) {
            log.error("Unknown set of parameters received", ex);
            CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                    communicationRequest, "Payload has unknown param(s)", "Inconsistent request");
            PushNotificationLog pushLog = new PushNotificationLog();
            pushLog.campaignName(campaignName).silentMode(silentMode)
                    .msisdn(keyValueParam.get("MSISDN")).endpointType(keyValueParam.get("ENDPOINT_TYPE")).communicationCode(offerId).
                    response(silentMode.equalsIgnoreCase(Y) ? "Silent Mode" : ("Unknown set of parameters received"))
                    .communicationUUID(communicationRequest.getCommunicationUUID());
            this.logRepository.save(pushLog);
            sendEvent(communicationResponse.toEvent());
            return communicationResponse;
        } catch (Exception e) {
            log.error("An unexpected error occurred. Request: {}", communicationRequest, e);
            PushNotificationLog pushLog = new PushNotificationLog();
            pushLog.campaignName(campaignName).silentMode(silentMode)
                    .msisdn(keyValueParam.get("MSISDN")).endpointType(keyValueParam.get("ENDPOINT_TYPE")).communicationCode(offerId).
                    response(silentMode.equalsIgnoreCase(Y) ? "Silent Mode" : ("An unexpected error occurred"))
                    .communicationUUID(communicationRequest.getCommunicationUUID());
            this.logRepository.save(pushLog);
            CommunicationResponse communicationExceptionResponse = generateFailCommunicationResponse(
                    communicationRequest, e.getMessage(), "UNEXPECTED");
            sendEvent(communicationExceptionResponse.toEvent());
            return communicationExceptionResponse;
        }
    }

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    private CommunicationResponse generateCommunicationResponse(
            CommunicationRequest communicationRequest) {
        return RANDOM.nextInt(2) == 0 ? generateSuccessCommunicationResponse(communicationRequest,
                UUID.randomUUID().toString(), "Success Message")
                : generateFailCommunicationResponse(communicationRequest, "Fail Message",
                        "FAIL");
    }
}

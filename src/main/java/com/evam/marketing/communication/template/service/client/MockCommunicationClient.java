package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.CustomCommunicationRequest;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.event.model.CommunicationResponseEvent;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by cemserit on 12.03.2021.
 */
@Service
@Slf4j
public class MockCommunicationClient extends AbstractCommunicationClient {

    private static final String PROVIDER = "MOCK_PROVIDER";
    private static final Random RANDOM = new Random();

    private static final long startTime = System.currentTimeMillis();
    public static long timesCalled = 0;

    @Autowired
    public MockCommunicationClient(KafkaProducerService kafkaProducerService) {
        super(kafkaProducerService);
    }

    @Override
    @RateLimiter(name = "client-limiter")
    @NotNull
    public CommunicationResponse send(CommunicationRequest communicationRequest) {

        CustomCommunicationRequest customCommunicationRequest = (CustomCommunicationRequest) communicationRequest;

        log.info("getting campaign name and offer id");
        String campaignName = customCommunicationRequest.getScenario();
        String offerId = customCommunicationRequest.getCommunicationCode();
        log.info("campaign name is {} and offer id is {}", campaignName, offerId);

        List<Parameter> parameters = customCommunicationRequest.getParameters();
        log.info("parameters received are {}", parameters);
        try {
            Map<String, String> keyValueParam = Helper.convertToKeyValue(parameters);
            String endpoint = Helper.identifyEndpoint(keyValueParam);
            log.info("{} - endpoint({}) - msisdn({})", keyValueParam, endpoint, keyValueParam.get("msisdn"));

            String silentMode = keyValueParam.get("SILENTMODE");
            String response = null;
            if (silentMode.equalsIgnoreCase("n")) {
                if (Helper.isBetween9AmAnd9Pm()) {
                    response = Helper.hit(endpoint);
                    log.info("response is {} ", response);
                } else {
                    response = "The request is received between 09:00AM and 09:00PM, therefore, the endpoint is not hit.";
                }
            }
            response = response == null ? silentMode : response;
            new DbLogging().populateDBLog(campaignName, offerId, silentMode, keyValueParam.get("msisdn"), "", "", "", "", "", "", "", "", "", "", "", "", "", "push", endpoint, response);

            CommunicationResponse communicationSuccessResponse = generateSuccessCommunicationResponse(
                    communicationRequest, "Push Response", "Invoking push service is completed successfully");
            sendEvent(communicationSuccessResponse.toEvent());
            return communicationSuccessResponse;

        } catch (UnknownPayloadException ex) {
            log.error("Unknown set of parameters received", ex);
            CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                    communicationRequest, "Payload has unknown param(s)", "Inconsistent request");
            sendEvent(communicationResponse.toEvent());
            return communicationResponse;
        } catch (Exception e) {
            log.error("An unexpected error occurred. Request: {}",
                    communicationRequest, e);
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

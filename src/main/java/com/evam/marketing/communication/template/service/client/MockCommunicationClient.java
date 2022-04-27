package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.client.model.CustomCommunicationRequest;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.event.model.CommunicationResponseEvent;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
        CommunicationResponse communicationResponse = generateCommunicationResponse(communicationRequest);

        try {

            /*
            https://jazzpk.atlassian.net/wiki/spaces/cms/pages/3117252633/EVAM+Designer+Offer+Communicator+Parameters
            */
            String msisdn = "";
            String silentMode = "";
            String message = "";
            //CampaignID = customCommunicationRequest.getCommunicationCode();
            //CampaignName = customCommunicationRequest.getScenario();
            log.info("Communicator triggered");
            List<Parameter> parameters = customCommunicationRequest.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                try {
                    if (parameters.get(i).getName().equalsIgnoreCase("MSISDN")) {
                        msisdn = parameters.get(i).getValue();
                        log.info("****** MSISDN ******" + msisdn);
                    } else if (parameters.get(i).getName().equalsIgnoreCase("MESSAGETEXT")) {
                        message = (parameters.get(i)).getValue();
                        log.info("****** MESSAGETEXT ******" + message);
                    } else if (parameters.get(i).getName().equalsIgnoreCase("SILENTMODE")) {
                        silentMode = (parameters.get(i)).getValue();
                        log.info("****** SILENTMODE ******" + silentMode);
                    } else {
                        log.info("Wrong parameter name {} , please check your parameters", (parameters.get(i)).getName());
                    }

                } catch (Exception ex) {
                    log.error("Exception on {}:{} ", msisdn, ex.getMessage());
                }
            }



        } catch (Exception e) {
            log.error("An unexpected error occurred. Request: {}",
                    communicationRequest, e);
            CommunicationResponse communicationExceptionResponse = generateFailCommunicationResponse(
                    communicationRequest, e.getMessage(), "UNEXPECTED");
            sendEvent(communicationExceptionResponse.toEvent());
            return communicationExceptionResponse;
        }

        return communicationResponse;
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

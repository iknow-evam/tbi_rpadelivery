package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.configuration.AppConfig;
import com.evam.marketing.communication.template.repository.status.model.StatusType;
import com.evam.marketing.communication.template.service.client.AbstractCommunicationClient;
import com.evam.marketing.communication.template.service.client.Helper;
import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.CustomCommunicationRequest;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;
import com.evam.marketing.communication.template.service.client.model.ServiceResponse;
import com.evam.marketing.communication.template.service.client.model.response.PushServiceResponse;
import com.evam.marketing.communication.template.service.client.model.response.SearchCustomerResponse;
import com.evam.marketing.communication.template.service.client.repository.LogRepository;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import com.sun.org.apache.xalan.internal.res.XSLTErrorResources;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class TbiMockCommunicationClient extends AbstractCommunicationClient {
    private static final String PROVIDER = "MOCK_PROVIDER";
    private static final Random RANDOM = new Random();
    //private static final String Y = "Y";
    //private static final String N = "N";

    //private final static String KEY = "KEY";
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String QUERY = "QUERY";
    private static final String QUERY_TYPE = "QUERY_TYPE";
    //private static final String SILENT_MODE = "SILENTMODE";

    @Autowired
    private LogRepository logRepository;

    private final Helper helper;


    public TbiMockCommunicationClient(KafkaProducerService kafkaProducerService, Helper helper) {
        super(kafkaProducerService);
        this.helper = helper;
    }

    //parameters=[Parameter(name=TITLE, value=Ново известие от TBI Bank , Ново известие от TBI Bank(DEFAULT TYPE)), Parameter(name=KEY_TYPE, value=userId), Parameter(name=BODY, value=Please push at the loan to sign it), Parameter(name=BADGE_COUNT, value=null), Parameter(name=LINK, value=null), Parameter(name=NOTIFICATION_TYPE, value=system), Parameter(name=DATA_TYPE, value=offer), Parameter(name=CAMPAIGN_ID, value=null), Parameter(name=DESCRIPTION, value=null), Parameter(name=QUERY, value=3.0), Parameter(name=QUERY_TYPE, value=p)])

    @Override
    //@RateLimiter(name = "client-limiter")
    @NotNull
    public CommunicationResponse send(CommunicationRequest communicationRequest) {
        PushNotificationLog.PushNotificationLogBuilder pushLog = PushNotificationLog.builder();
        CustomCommunicationRequest customCommunicationRequest = (CustomCommunicationRequest) communicationRequest;

        String campaignName = customCommunicationRequest.getScenario();
        String offerId = customCommunicationRequest.getCommunicationCode();
        log.debug("campaign name is {} and offer id is {}", campaignName, offerId);

        List<Parameter> parameters = customCommunicationRequest.getParameters();
        log.debug("parameters received are {}", parameters);
        Map<String, String> keyValueParam = convertToKeyValue(parameters);
        ServiceResponse serviceResponse = null;
        SearchCustomerResponse searchCustomerResponse = null;
        try {
            searchCustomerResponse = helper.callSearchCustomerService(keyValueParam.get(QUERY), keyValueParam.get(QUERY_TYPE));
            if (searchCustomerResponse != null && searchCustomerResponse.getUserId() != null) {
                log.info("Search Customer API is SUCCESSFULLY, USER_ID {}", searchCustomerResponse.getUserId());
                serviceResponse = helper.callPushService(keyValueParam, searchCustomerResponse.getUserId());
                if (serviceResponse == null) {
                    serviceResponse.setMsg("There is no response from server");
                } else {
                    serviceResponse.setMsg(serviceResponse.getResponse().getNotificationId());
                    serviceResponse.setResponse(PushServiceResponse.builder().notificationId(serviceResponse.getResponse().getNotificationId()).build());
                }
                log.debug("response after hitting endpoint is {} ", serviceResponse);

                pushLog.campaignName(campaignName)
                        .userId(searchCustomerResponse.getUserId())
                        .endpointType(keyValueParam.get(DATA_TYPE))
                        .communicationCode(offerId)
                        .response(serviceResponse.getResponse().getNotificationId())
                        .request(serviceResponse.getRequest().toString())
                        .communicationUUID(communicationRequest.getCommunicationUUID())
                        .actorId(communicationRequest.getActorId())
                        .status(StatusType.SENT.name());
                this.logRepository.save(pushLog.build());

                CommunicationResponse communicationSuccessResponse = generateSuccessCommunicationResponse(
                        communicationRequest, "Push Response", serviceResponse.getMsg());
                sendEvent(communicationSuccessResponse.toEvent());
                return communicationSuccessResponse;
            } else {
                pushLog.campaignName(campaignName)
                        .endpointType(keyValueParam.get(DATA_TYPE))
                        .communicationCode(offerId)
                        .response("User Id Not Found")
                        .communicationUUID(communicationRequest.getCommunicationUUID())
                        .actorId(communicationRequest.getActorId())
                        .status(StatusType.FAILED.name());
                this.logRepository.save(pushLog.build());

                CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                        communicationRequest, "Payload has unknown param(s)",  "User Id Not Found");
                sendEvent(communicationResponse.toEvent());
                return communicationResponse;
            }


        } catch (UnknownPayloadException ex) {
            log.error("Unknown set of parameters received", ex);
            CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                    communicationRequest, "Payload has unknown param(s)", "Inconsistent request");
            pushLog.campaignName(campaignName)
                    .userId(searchCustomerResponse.getUserId() != null ? searchCustomerResponse.getUserId() : "null")
                    .endpointType(keyValueParam.get(DATA_TYPE))
                    .communicationCode(offerId)
                    .request(serviceResponse != null ? serviceResponse.getRequest().toString() : "null")
                    .response(ex.getMessage())
                    .communicationUUID(communicationRequest.getCommunicationUUID())
                    .actorId(communicationRequest.getActorId())
                    .status(StatusType.FAILED.name());
            this.logRepository.save(pushLog.build());
            sendEvent(communicationResponse.toEvent());
            return communicationResponse;
        } catch (Exception e) {
            log.error("An unexpected error occurred. Request: {}", communicationRequest, e);
            CommunicationResponse communicationExceptionResponse = generateFailCommunicationResponse(
                    communicationRequest, e.getMessage(), "UNEXPECTED");
            pushLog.campaignName(campaignName)
                    .userId(searchCustomerResponse.getUserId() != null ? searchCustomerResponse.getUserId() : "null")
                    .endpointType(keyValueParam.get(DATA_TYPE))
                    .communicationCode(offerId)
                    .response(e.getMessage())
                    .request(serviceResponse != null ? serviceResponse.getRequest().toString() : "null")
                    .communicationUUID(communicationRequest.getCommunicationUUID())
                    .actorId(communicationRequest.getActorId())
                    .status(StatusType.FAILED.name());
            ;
            this.logRepository.save(pushLog.build());
            sendEvent(communicationExceptionResponse.toEvent());
            return communicationExceptionResponse;
        }
    }

    @Override
    public String getProvider() {
        return PROVIDER;
    }


    public static Map<String, String> convertToKeyValue(List<Parameter> params) {
        Map<String, String> keyValuePairs = new HashMap<>();
        for (Parameter param : params) {
            keyValuePairs.put(param.getName(), param.getValue());
        }
        return keyValuePairs;
    }
}

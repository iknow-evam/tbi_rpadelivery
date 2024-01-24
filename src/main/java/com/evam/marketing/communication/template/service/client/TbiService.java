package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.configuration.AppConfig;
import com.evam.marketing.communication.template.repository.status.model.StatusType;
import com.evam.marketing.communication.template.service.client.model.CustomCommunicationRequest;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;
import com.evam.marketing.communication.template.service.client.repository.LogRepository;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import com.evam.marketing.communication.template.utils.BlackHourControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class TbiService extends AbstractCommunicationClient {
    private static final String DATA_TYPE = "DATA_TYPE";
    private static final String TIME_CONSTRAINT = "TIME_CONSTRAINT";

    private final LogRepository logRepository;
    private final WorkerService workerService;
    private final AppConfig appConfig;

    private final PushNotificationLog.PushNotificationLogBuilder pushLog = PushNotificationLog.builder();

    public TbiService(KafkaProducerService kafkaProducerService,
                      WorkerService workerService,
                      LogRepository logRepository,
                      AppConfig appConfig
    ) {
        super(kafkaProducerService);
        this.workerService = workerService;
        this.logRepository = logRepository;
        this.appConfig = appConfig;
    }

    @Override
    public void send(CommunicationRequest communicationRequest) {
        LocalDateTime now = LocalDateTime.now();
        try {
            CustomCommunicationRequest customCommunicationRequest = (CustomCommunicationRequest) communicationRequest;
            List<Parameter> parameters = customCommunicationRequest.getParameters();
            log.debug("parameters received are {}", parameters);
            Map<String, String> keyValueParam = convertToKeyValue(parameters);
            if (keyValueParam.get(TIME_CONSTRAINT).equalsIgnoreCase("true")) {
                if (!BlackHourControl.isTimeWindowOk(appConfig.getSILENT_MODE_START(), appConfig.getSILENT_MODE_END(), now)) {
                    log.debug("Given time is not between time window[{}]", customCommunicationRequest);
                    pushLog.campaignName(communicationRequest.getScenario())
                            .endpointType(keyValueParam.get(DATA_TYPE))
                            .communicationCode(communicationRequest.getCommunicationCode())
                            .response("TIME_CONSTRAINT")
                            .communicationUUID(communicationRequest.getCommunicationUUID())
                            .actorId(communicationRequest.getActorId())
                            .status(StatusType.FAILED.name());
                    this.logRepository.save(pushLog.build());

                    CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                            communicationRequest, "TIME_CONSTRAINT", "Given time is not between time window");
                    sendEvent(communicationResponse.toEvent());
                } else {
                    workerService.callPushService(communicationRequest, keyValueParam);
                }
            } else {
                workerService.callPushService(communicationRequest, keyValueParam);
            }
        } catch (Exception e) {
            log.error("Error occurred while handling request: {}", communicationRequest, e);
        }

    }

    public static Map<String, String> convertToKeyValue(List<Parameter> params) {
        Map<String, String> keyValuePairs = new HashMap<>();
        for (Parameter param : params) {
            keyValuePairs.put(param.getName(), param.getValue());
        }
        return keyValuePairs;
    }
}

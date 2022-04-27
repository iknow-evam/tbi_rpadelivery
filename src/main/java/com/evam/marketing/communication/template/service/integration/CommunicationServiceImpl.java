package com.evam.marketing.communication.template.service.integration;

import com.evam.marketing.communication.template.repository.status.model.CustomCommunicationStatus;
import com.evam.marketing.communication.template.repository.template.model.ResourceTemplate;
import com.evam.marketing.communication.template.service.client.CommunicationClient;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import com.evam.marketing.communication.template.service.status.CommunicationStatusService;
import com.evam.marketing.communication.template.service.stream.model.request.StreamRequest;
import com.evam.marketing.communication.template.service.template.ResourceTemplateService;
import com.evam.marketing.communication.template.utils.CommunicationConversionUtils;
import com.evam.marketing.communication.template.utils.ResourceTemplateUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by cemserit on 11.03.2021.
 */
@Service
@Slf4j
public class CommunicationServiceImpl implements CommunicationService {

    private final CommunicationClient communicationClient;
    private final ResourceTemplateService resourceTemplateService;
    private final CommunicationStatusService communicationStatusService;

    @Value("${kafka.communication-uuid-check:false}")
    private boolean communicationUUIDCheck;

    public CommunicationServiceImpl(CommunicationClient communicationClient,
            ResourceTemplateService resourceTemplateService,
            CommunicationStatusService communicationStatusService) {
        this.communicationClient = communicationClient;
        this.resourceTemplateService = resourceTemplateService;
        this.communicationStatusService = communicationStatusService;
    }

    @Override
    public List<CommunicationResponse> execute(List<StreamRequest> requestList) {
        Collection<String> communicationUuidList = requestList.stream()
                .map(StreamRequest::getUuid).collect(Collectors.toList());

        Set<String> alreadySentList = Collections.emptySet();

        if (communicationUUIDCheck) {
            alreadySentList = communicationStatusService.getCommunicationStatus(
                    communicationUuidList);
        }

        final Set<String> finalAlreadySentList = alreadySentList;

        List<CommunicationResponse> communicationResponses = new ArrayList<>();

        for (StreamRequest streamRequest : requestList) {
            try {
                if (finalAlreadySentList.contains(streamRequest.getUuid())) {
                    log.warn("Already sent request skipped {}.", streamRequest);
                    continue;
                }
                CommunicationRequest communicationRequest = generateCommunicationRequest(
                        streamRequest);
                CommunicationResponse communicationResponse = communicationClient.send(
                        communicationRequest);

                communicationResponses.add(communicationResponse);
            } catch (Exception e) {
                log.error("Unexpected error occurred! Request: {}", streamRequest, e);
            }
        }

        storeStatus(communicationResponses);
        return communicationResponses;
    }

    private CommunicationRequest generateCommunicationRequest(StreamRequest streamRequest) {
        Optional<ResourceTemplate> resourceTemplateOptional = Optional.empty();
        if (streamRequest.hasResource()) {
            resourceTemplateOptional = resourceTemplateService.getResourceTemplate(
                    streamRequest.getCode(),
                    streamRequest.getScenario(), streamRequest.getScenarioVersion());
        }
        String body = null;
        if (resourceTemplateOptional.isPresent() && !Objects.isNull(
                resourceTemplateOptional.get().getContent())) {
            ResourceTemplate resourceTemplate = resourceTemplateOptional.get();
            body = ResourceTemplateUtils.enrichContent(streamRequest,
                    resourceTemplate.getContent());
        }
        return CommunicationConversionUtils.streamRequestToCommunicationRequest(streamRequest,
                body);
    }

    private void storeStatus(List<CommunicationResponse> communicationResponses) {
        try {
            List<CustomCommunicationStatus> statuses = communicationResponses.stream()
                    .map(CommunicationResponse::toCommunicationStatus)
                    .collect(Collectors.toList());

            communicationStatusService.saveBatchCommunicationStatus(statuses);
            log.debug("Custom communication bulk status successfully stored. {}", statuses);
        } catch (Exception e) {
            log.warn("Unexpected error occurred while store bulk custom communication status! {}",
                    communicationResponses, e);
        }
    }

}
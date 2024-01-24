package com.evam.marketing.communication.template.service.integration;

import com.evam.marketing.communication.template.repository.template.model.ResourceTemplate;
import com.evam.marketing.communication.template.service.client.TbiService;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.stream.model.request.StreamRequest;
import com.evam.marketing.communication.template.service.template.ResourceTemplateService;
import com.evam.marketing.communication.template.utils.CommunicationConversionUtils;
import com.evam.marketing.communication.template.utils.PerformanceCounter;
import com.evam.marketing.communication.template.utils.ResourceTemplateUtils;

import java.time.LocalDate;
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

    private final TbiService TbiService;
    private final ResourceTemplateService resourceTemplateService;
    private final CustomCommunicationLogService customCommunicationLogService;
    private final PerformanceCounter performanceCounter;

    @Value("${kafka.communication-uuid-check:false}")
    private boolean communicationUUIDCheck;

    public CommunicationServiceImpl(TbiService TbiService,
                                    ResourceTemplateService resourceTemplateService,
                                    CustomCommunicationLogService customCommunicationLogService,
                                    PerformanceCounter performanceCounter) {
        this.TbiService = TbiService;
        this.resourceTemplateService = resourceTemplateService;
        this.customCommunicationLogService = customCommunicationLogService;
        this.performanceCounter = performanceCounter;
    }

    @Override
    public void execute(List<StreamRequest> requestList) {
        Collection<String> communicationUuidList = requestList.stream()
                .map(StreamRequest::getUuid).collect(Collectors.toList());

        Set<String> alreadySentList = Collections.emptySet();

        if (communicationUUIDCheck) {
            alreadySentList = customCommunicationLogService.findByCommunicationUUIDIn(LocalDate.now(),
                    communicationUuidList);
        }

        final Set<String> finalAlreadySentList = alreadySentList;

        //List<CommunicationResponse> communicationResponses = new ArrayList<>();

        for (StreamRequest streamRequest : requestList) {
            try {
                if (finalAlreadySentList.contains(streamRequest.getUuid())) {
                    log.warn("Already sent request skipped {}.", streamRequest);
                    performanceCounter.incrementBatchCountDuplicate();
                    continue;
                }
                CommunicationRequest communicationRequest = generateCommunicationRequest(
                        streamRequest);
                TbiService.send(communicationRequest);

                //communicationResponses.add(communicationResponse);
                performanceCounter.incrementBatchCountSuccess();

            } catch (Exception e) {
                log.error("Unexpected error occurred! Request: {}", streamRequest, e);
                performanceCounter.incrementBatchCountError();
            }
        }
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
}
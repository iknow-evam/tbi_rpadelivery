package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;

/**
 * Created by cemserit on 12.03.2021.
 */
public interface CommunicationClient {

    void send(CommunicationRequest communicationRequest);
}

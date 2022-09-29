package com.evam.marketing.communication.template.service.integration;

import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Created by cemserit on 11.03.2021.
 */
public interface CustomCommunicationLogService {

    void save(PushNotificationLog log);

    Set<String> findByCommunicationUUIDIn(LocalDate submitDate,
        Collection<String> communicationUUIDS);
}

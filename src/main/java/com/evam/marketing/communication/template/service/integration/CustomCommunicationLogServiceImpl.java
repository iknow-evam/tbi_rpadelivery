package com.evam.marketing.communication.template.service.integration;

import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;
import com.evam.marketing.communication.template.service.client.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomCommunicationLogServiceImpl implements
    CustomCommunicationLogService {

    private final LogRepository repository;

    @Override
    public void save(PushNotificationLog log) {
        repository.save(log);
    }

    @Override
    public Set<String> findByCommunicationUUIDIn(LocalDate submitDate,
        Collection<String> communicationUUIDS) {
        return repository.findByCommunicationUUIDIn(submitDate, communicationUUIDS);
    }
}

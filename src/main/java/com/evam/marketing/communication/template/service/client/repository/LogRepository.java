package com.evam.marketing.communication.template.service.client.repository;

import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

public interface LogRepository extends JpaRepository<PushNotificationLog, Integer> {
    @Query("select log.communicationUUID as communicationUUID from PushNotificationLog log where log.submitDate=:submitDate and communicationUUID in(:communicationUUIDSet)")
    Set<String> findByCommunicationUUIDIn(@Param("submitDate") LocalDate submitDate,
                                          @Param("communicationUUIDSet") Collection<String> communicationUUIDSet);
}

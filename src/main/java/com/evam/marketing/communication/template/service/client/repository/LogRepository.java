package com.evam.marketing.communication.template.service.client.repository;

import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<PushNotificationLog, Integer> {

}

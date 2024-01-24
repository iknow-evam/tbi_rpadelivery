package com.evam.marketing.communication.template.service.client.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.*;

import com.evam.marketing.communication.template.repository.status.model.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.lang.Nullable;

@Data
@Entity
@Table(name = "LOG_PUSH_NOTIFICATION")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true, chain = true)
@Builder(toBuilder = true)
public class PushNotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "LOG_PUSH_NOTIFICATION_SEQUENCE")
    private long id;

    @CreationTimestamp
    private LocalDateTime insertTime;

    private String actorId;
    private String campaignName;
    private String communicationCode;
    //private String silentMode;
    @Nullable
    private String userId;
    private String endpointType;
    @Lob
    private String response;
    @Lob
    private String request;
    private String status;
    @CreationTimestamp
    private LocalDate submitDate;
    @Column(name = "COMMUNICATION_UUID")
    private String communicationUUID;
}

package com.evam.marketing.communication.template.service.client.model.request.concretes;

import com.evam.marketing.communication.template.service.client.model.request.abstracts.DataType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest implements Serializable {
    private String body;
    private String title;
    private DataType data;
}

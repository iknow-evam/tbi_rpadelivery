package com.evam.marketing.communication.template.service.client.model.response;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushServiceResponse implements Serializable {
    //public String resultCode;
    //public String msg;
    private String notificationId;
}

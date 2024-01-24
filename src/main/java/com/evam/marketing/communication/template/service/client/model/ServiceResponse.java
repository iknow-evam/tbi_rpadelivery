package com.evam.marketing.communication.template.service.client.model;

import com.evam.marketing.communication.template.service.client.model.response.PushServiceResponse;
import com.evam.marketing.communication.template.service.client.model.response.SearchCustomerResponse;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResponse {
    private Object request;
    private String response;
    private String msg;
}

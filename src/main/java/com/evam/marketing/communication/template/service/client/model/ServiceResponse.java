package com.evam.marketing.communication.template.service.client.model;

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

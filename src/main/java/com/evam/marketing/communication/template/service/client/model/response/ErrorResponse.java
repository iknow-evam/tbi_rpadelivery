package com.evam.marketing.communication.template.service.client.model.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
@Getter
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JsonCreator)
public class ErrorResponse extends RuntimeException{

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;
}

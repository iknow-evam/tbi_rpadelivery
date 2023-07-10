package com.evam.marketing.communication.template.service.client.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchCustomerResponse {

    @JsonProperty("EGN")
    private String EGN;
    @JsonProperty("MobilePhoneLocal")
    private String MobilePhoneLocal;
    @JsonProperty("MobilePhoneInternational")
    private String MobilePhoneInternational;
    @JsonProperty("IsPhoneValidated")
    private boolean IsPhoneValidated;
    @JsonProperty("userId")
    private String userId;

}

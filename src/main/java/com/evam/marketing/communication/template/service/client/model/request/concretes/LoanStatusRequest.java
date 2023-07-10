package com.evam.marketing.communication.template.service.client.model.request.concretes;

import com.evam.marketing.communication.template.service.client.model.request.abstracts.DataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class LoanStatusRequest extends DataType implements Serializable {
    private String applicationId;
    private String status;
}

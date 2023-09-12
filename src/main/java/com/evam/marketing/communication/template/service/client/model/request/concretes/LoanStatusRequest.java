package com.evam.marketing.communication.template.service.client.model.request.concretes;

import com.evam.marketing.communication.template.service.client.model.request.abstracts.DataType;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class LoanStatusRequest extends DataType implements Serializable {
    private String applicationId;
    private String status;
}

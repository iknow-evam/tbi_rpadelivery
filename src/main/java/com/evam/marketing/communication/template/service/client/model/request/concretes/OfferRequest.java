package com.evam.marketing.communication.template.service.client.model.request.concretes;

import com.evam.marketing.communication.template.service.client.model.request.abstracts.DataType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
public class OfferRequest extends DataType implements Serializable {
}

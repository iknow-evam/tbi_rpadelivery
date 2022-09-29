package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.configuration.AppConfig;
import com.evam.marketing.communication.template.service.client.ex.UnknownPayloadException;
import com.evam.marketing.communication.template.service.client.model.Parameter;
import com.evam.marketing.communication.template.service.client.model.PushType;
import com.evam.marketing.communication.template.service.client.model.ServiceResponse;
import com.evam.marketing.communication.template.service.client.model.request.BonusOfferPushRequest;
import com.evam.marketing.communication.template.service.client.model.request.LinkPushRequest;
import com.evam.marketing.communication.template.service.client.model.request.OfferPushRequest;
import com.evam.marketing.communication.template.service.client.model.request.SimplePushRequest;
import com.evam.marketing.communication.template.service.client.model.response.PushServiceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class Helper {
    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;

    public Helper(RestTemplate restTemplate, HttpHeaders httpHeaders) {
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
    }

    public static Map<String, String> convertToKeyValue(List<Parameter> params) {
        Map<String, String> keyValuePairs = new HashMap<>();
        for (Parameter param : params) {
            keyValuePairs.put(param.getName(), param.getValue());
        }
        return keyValuePairs;
    }

    public HttpHeaders getHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("4104f6e6-e091-3cc0-a163-41a9ebecac16");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept((List<MediaType>) MediaType.APPLICATION_JSON);
        return headers;
    }

    public ServiceResponse webServiceCall(AppConfig appConfig, Map<String, String> p) throws Exception {
        log.info("identifying use case for the payload ({})", p);
        if (p.containsKey("ENDPOINT_TYPE")) {
            String dataTypeStr = p.get("ENDPOINT_TYPE");
            log.debug(dataTypeStr);
            PushType pushType = PushType.fromString(dataTypeStr);
            if (!(pushType == null)) {
                switch (Objects.requireNonNull(pushType)) {
                    case LINK_PUSH:
                        log.debug("LINK_PUSH");
                        LinkPushRequest linkPushRequest = LinkPushRequest.builder().msisdn(p.get("MSISDN")).deepUrl(p.get("DEEPURL")).notificationText(p.get("NOTIFICATIONTEXT"))
                                .notificationTitle(p.get("NOTIFICATIONTITLE")).urlIdentifier(p.get("URLIDENTIFIER")).build();
                        HttpEntity<LinkPushRequest> linkPushRequestHttpEntity = new HttpEntity<>(linkPushRequest, httpHeaders);
                        ResponseEntity<PushServiceResponse> linkPushResponse = restTemplate.exchange(appConfig.notifyLinkUrl, HttpMethod.POST, linkPushRequestHttpEntity, PushServiceResponse.class);
                        if (linkPushResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(linkPushRequest).response(linkPushResponse.getBody()).build();
                        }
                        return null;
                    case OFFER_PUSH:
                        log.debug("OFFER_PUSH");
                        OfferPushRequest offerPushRequest = OfferPushRequest.builder().offerPayload(p.get("OFFERPAYLOAD")).deepUrl(p.get("DEEPURL")).msisdn(p.get("MSISDN")).notificationText(p.get("NOTIFICATIONTEXT"))
                                .notificationTitle(p.get("NOTIFICATIONTITLE")).sessionID(p.get("SESSIONID")).uACIInteractionPointName(p.get("INTERACTIONPOINTNAME")).uACIInteractiveChannelname(p.get("INTERACTIVECHANNELNAME"))
                                .urlIdentifier(p.get("URLIDENTIFIER")).build();
                        HttpEntity<OfferPushRequest> offerPushRequestHttpEntity = new HttpEntity<>(offerPushRequest, httpHeaders);
                        ResponseEntity<PushServiceResponse> offerPushResponse = restTemplate.exchange(appConfig.notifyOfferUrl, HttpMethod.POST, offerPushRequestHttpEntity, PushServiceResponse.class);
                        if (offerPushResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(offerPushRequest).response(offerPushResponse.getBody()).build();
                        }
                        return null;
                    case SIMPLE_PUSH:
                        log.debug("SIMPLE_PUSH");
                        SimplePushRequest simplePushRequest = SimplePushRequest.builder().fcmId(p.get("FCMID")).msisdn(p.get("MSISDN")).notificationType(p.get("NOTIFICATIONTYPE")).status(p.get("STATUS")).build();
                        HttpEntity<SimplePushRequest> simplePushRequestHttpEntity = new HttpEntity<>(simplePushRequest, httpHeaders);
                        ResponseEntity<PushServiceResponse> simplePushResponse = restTemplate.exchange(appConfig.notifyUrl, HttpMethod.POST, simplePushRequestHttpEntity, PushServiceResponse.class);
                        if (simplePushResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(simplePushRequest).response(simplePushResponse.getBody()).build();
                        }
                        return null;
                    case BONUSOFFER_PUSH:
                        log.debug("BONUSOFFER_PUSH");
                        BonusOfferPushRequest bonusOfferPushRequest = BonusOfferPushRequest.builder().status(p.get("STATUS")).msisdn(p.get("MSISDN")).notificationTitle(p.get("NOTIFICATIONTITLE"))
                                .notificationText(p.get("NOTIFICATIONTEXT")).commercialText(p.get("COMMERCIALTEXT")).offerPayload(p.get("OFFERPAYLOAD")).build();
                        HttpEntity<BonusOfferPushRequest> bonusOfferPushRequestHttpEntity = new HttpEntity<>(bonusOfferPushRequest, httpHeaders);
                        ResponseEntity<PushServiceResponse> bonusOfferResponse = restTemplate.exchange(appConfig.notifyBonusUrl, HttpMethod.POST, bonusOfferPushRequestHttpEntity, PushServiceResponse.class);
                        if (bonusOfferResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(bonusOfferPushRequest).response(bonusOfferResponse.getBody()).build();
                        }
                        return null;
                    default:
                        log.error("ENDPOINT_TYPE does not match any of the given parameters! ENDPOINT_TYPE : {}", p.get("ENDPOINT_TYPE"));
                        return null;
                }
            }
        }
        return null;
    }

    public static boolean isTimeBetween(String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        LocalTime now = LocalTime.now();

        return start.isBefore(now) && end.isAfter(now);
    }
}

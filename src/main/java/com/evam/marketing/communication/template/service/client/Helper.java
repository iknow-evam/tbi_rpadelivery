package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.configuration.AppConfig;
import com.evam.marketing.communication.template.configuration.SearchCustomerAppConfig;
import com.evam.marketing.communication.template.service.client.model.PushType;
import com.evam.marketing.communication.template.service.client.model.ServiceResponse;
import com.evam.marketing.communication.template.service.client.model.request.abstracts.DataType;
import com.evam.marketing.communication.template.service.client.model.request.concretes.*;
import com.evam.marketing.communication.template.service.client.model.response.PushServiceResponse;
import com.evam.marketing.communication.template.service.client.model.response.SearchCustomerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Slf4j
public class Helper {
    private final RestTemplate restTemplate;
    private final HttpHeaders httpHeaders;
    private final AppConfig appConfig;
    private final SearchCustomerAppConfig searchCustomerAppConfig;
    private final ObjectMapper objectMapper;

    private static final String KEY = "KEY";
    private static final String KEY_TYPE = "KEY_TYPE";
    private static final String NOTIFICATION_TYPE = "NOTIFICATION_TYPE";
    private static final String BODY = "BODY";
    private static final String TITLE = "TITLE";

    private static final String CAMPAIGN_ID = "CAMPAIGN_ID";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String BADGE_COUNT = "BADGE_COUNT";
    private static final String STATUS = "STATUS";
    private static final String TRANSACTION_ID = "TRANSACTION_ID";
    private static final String SUCCESSFUL_PAYMENT = "SUCCESSFUL_PAYMENT";
    private static final String REWARD_STATUS = "REWARD_STATUS";

    private static final String LINK = "LINK";
    private static final String DATA_TYPE = "DATA_TYPE";

    private Duration timeElapsed = null;

    public Helper(RestTemplate restTemplate, HttpHeaders httpHeaders, AppConfig appConfig, SearchCustomerAppConfig searchCustomerAppConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
        this.appConfig = appConfig;
        this.searchCustomerAppConfig = searchCustomerAppConfig;
        this.objectMapper = objectMapper;
    }

    public ServiceResponse callPushService(Map<String, String> p, String userId) throws Exception {
        log.info("identifying use case for the payload ({})", p);
        if (p.containsKey(DATA_TYPE)) {
            String dataTypeStr = p.get(DATA_TYPE);
            log.debug(dataTypeStr);
            PushType pushType = PushType.fromString(dataTypeStr);

            if (!(pushType == null)) {
                Instant httpResponseStart = null;
                Instant httpResponseStop = null;
                NotificationRequest.NotificationRequestBuilder notificationRequest = NotificationRequest.builder();
                notificationRequest.body(p.get(BODY));
                notificationRequest.title(p.get(TITLE));

                BaseNotificationRequest baseNotificationRequest = BaseNotificationRequest.builder()
                        .key(userId)
                        .keyType(p.get(KEY_TYPE))
                        .notificationType(p.get(NOTIFICATION_TYPE))
                        .notification(notificationRequest.build())
                        .build();
                switch (Objects.requireNonNull(pushType)) {

                    case OFFER_PUSH:
                    case DEPOSIT_OPENING:
                    case FINANCIAL_OPERATIONS:
                    case NON_FINANCIAL_OPERATIONS:
                    case OTHER:
                    case KASICHKA:
                    case CDL_LIMIT_OFFER:
                    case UTILITY_PAYMENT:
                    case BNPL_CONTRACT_SIGN:
                        log.debug("Push Type {}",pushType.getValue());
                        DataType offerPush = OfferRequest.builder().type(pushType.getValue()).build();
                        notificationRequest.data(offerPush);
                        baseNotificationRequest.setNotification(notificationRequest.build());
                        log.debug("Push Request {}", baseNotificationRequest.toString());
                        HttpEntity<BaseNotificationRequest> offerPushRequestHttpEntity = new HttpEntity<>(baseNotificationRequest, httpHeaders);
                        httpResponseStart = Instant.now();
                        ResponseEntity<PushServiceResponse> offerPushResponse = restTemplate.exchange(appConfig.getAPI_URL(), HttpMethod.POST, offerPushRequestHttpEntity, PushServiceResponse.class);
                        httpResponseStop = Instant.now();
                        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
                        log.info("PUSH_API : Http Response Time Length {} ", timeElapsed.toMillis());
                        log.info("API RESPONSE {}, HEADERS {}", offerPushResponse, offerPushResponse.getHeaders());
                        if (offerPushResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(offerPush).response(offerPushResponse.getBody()).build();
                        }
                        return null;

                    case LINK_PUSH:
                        log.debug("Push Type {}",pushType.getValue());
                        DataType linkPushRequest = LinkRequest.builder().type(pushType.getValue()).link(p.get(LINK)).badgeCount(p.get(BADGE_COUNT)).build();
                        notificationRequest.data(linkPushRequest);
                        baseNotificationRequest.setNotification(notificationRequest.build());
                        log.debug("LINK Request Body {} ", baseNotificationRequest.toString());
                        HttpEntity<BaseNotificationRequest> linkPushRequestHttpEntity = new HttpEntity<>(baseNotificationRequest, httpHeaders);
                        httpResponseStart = Instant.now();
                        ResponseEntity<PushServiceResponse> linkPushResponse = restTemplate.exchange(appConfig.getAPI_URL(), HttpMethod.POST, linkPushRequestHttpEntity, PushServiceResponse.class);
                        httpResponseStop = Instant.now();
                        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
                        log.info("PUSH_API : Http Response Time Length {} ", timeElapsed.toMillis());
                        log.info("API RESPONSE {}, HEADERS {}", linkPushResponse, linkPushResponse.getHeaders());
                        if (linkPushResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(linkPushRequest).response(linkPushResponse.getBody()).build();
                        }
                        return null;

                    case REPAYMENT_PUSH:
                        log.debug("Push Type {}",pushType.getValue());
                        DataType repayment = RepaymentRequest.builder().type(pushType.getValue()).campaignId(p.get(CAMPAIGN_ID)).description(p.get(DESCRIPTION)).badgeCount(p.get(BADGE_COUNT)).build();
                        notificationRequest.data(repayment);
                        baseNotificationRequest.setNotification(notificationRequest.build());
                        log.debug("Repayment Request {}", baseNotificationRequest.toString());
                        HttpEntity<BaseNotificationRequest> simplePushRequestHttpEntity = new HttpEntity<>(baseNotificationRequest, httpHeaders);
                        httpResponseStart = Instant.now();
                        ResponseEntity<PushServiceResponse> simplePushResponse = restTemplate.exchange(appConfig.getAPI_URL(), HttpMethod.POST, simplePushRequestHttpEntity, PushServiceResponse.class);
                        httpResponseStop = Instant.now();
                        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
                        log.info("PUSH_API : Http Response Time Length {} ", timeElapsed.toMillis());
                        log.info("API RESPONSE {}, HEADERS {}", simplePushResponse, simplePushResponse.getHeaders());
                        if (simplePushResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(repayment).response(simplePushResponse.getBody()).build();
                        }
                        return null;

                    case DEPOSIT:
                        log.debug("Push Type {}",pushType.getValue());
                        DataType deposit = DepositRequest.builder().type(pushType.getValue()).status(p.get(STATUS)).build();
                        notificationRequest.data(deposit);
                        baseNotificationRequest.setNotification(notificationRequest.build());
                        log.debug("DEPOSIT Request {}", baseNotificationRequest.toString());
                        HttpEntity<BaseNotificationRequest> depositRequestEntity = new HttpEntity<>(baseNotificationRequest, httpHeaders);
                        httpResponseStart = Instant.now();
                        ResponseEntity<PushServiceResponse> depositResponse = restTemplate.exchange(appConfig.getAPI_URL(), HttpMethod.POST, depositRequestEntity, PushServiceResponse.class);
                        httpResponseStop = Instant.now();
                        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
                        log.info("PUSH_API : Http Response Time Length {} ", timeElapsed.toMillis());
                        log.info("API RESPONSE {}, HEADERS {}", depositResponse, depositResponse.getHeaders());
                        if (depositResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(deposit).response(depositResponse.getBody()).build();
                        }
                        return null;

                    case BNPL_OFFER:
                        log.debug("Push Type {}",pushType.getValue());
                        DataType bnplOffer = BNPLOfferRequest.builder().type(pushType.getValue()).transactionId(p.get(TRANSACTION_ID)).badgeCount(p.get(BADGE_COUNT)).build();
                        notificationRequest.data(bnplOffer);
                        baseNotificationRequest.setNotification(notificationRequest.build());
                        log.debug("BNPL Offer Request {}", baseNotificationRequest.toString());
                        HttpEntity<BaseNotificationRequest> bnplOfferEntity = new HttpEntity<>(baseNotificationRequest, httpHeaders);
                        httpResponseStart = Instant.now();
                        ResponseEntity<PushServiceResponse> bnplOfferResponse = restTemplate.exchange(appConfig.getAPI_URL(), HttpMethod.POST, bnplOfferEntity, PushServiceResponse.class);
                        httpResponseStop = Instant.now();
                        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
                        log.info("PUSH_API : Http Response Time Length {} ", timeElapsed.toMillis());
                        log.info("API RESPONSE {}, HEADERS {}", bnplOfferResponse, bnplOfferResponse.getHeaders());
                        if (bnplOfferResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(bnplOffer).response(bnplOfferResponse.getBody()).build();
                        }
                        return null;

                    case DEBIT_CARD:
                        log.debug("Push Type {}",pushType.getValue());
                        DataType debitCard = DebitCardRequest.builder().type(pushType.getValue()).badgeCount(p.get(BADGE_COUNT)).build();
                        notificationRequest.data(debitCard);
                        baseNotificationRequest.setNotification(notificationRequest.build());
                        log.debug("Debit Card Request {}", baseNotificationRequest.toString());
                        HttpEntity<BaseNotificationRequest> debitCardRequestEntity = new HttpEntity<>(baseNotificationRequest, httpHeaders);
                        httpResponseStart = Instant.now();
                        ResponseEntity<PushServiceResponse> debitCardResponse = restTemplate.exchange(appConfig.getAPI_URL(), HttpMethod.POST, debitCardRequestEntity, PushServiceResponse.class);
                        httpResponseStop = Instant.now();
                        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
                        log.info("PUSH_API : Http Response Time Length {} ", timeElapsed.toMillis());
                        log.info("API RESPONSE {}, HEADERS {}", debitCardResponse, debitCardResponse.getHeaders());
                        if (debitCardResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(debitCard).response(debitCardResponse.getBody()).build();
                        }
                        return null;
                    case REWARD:
                        log.debug("Push Type {}",pushType.getValue());
                        DataType rewardType = RewardRequest.builder().type(pushType.getValue()).rewardStatus(p.get(REWARD_STATUS)).build();
                        notificationRequest.data(rewardType);
                        baseNotificationRequest.setNotification(notificationRequest.build());
                        log.debug("Reward Request {}", baseNotificationRequest.toString());
                        HttpEntity<BaseNotificationRequest> rewardRequestEntity = new HttpEntity<>(baseNotificationRequest, httpHeaders);
                        log.info("RewardRequestEntity {}", rewardRequestEntity);
                        httpResponseStart = Instant.now();
                        ResponseEntity<PushServiceResponse> rewardResponse = restTemplate.exchange(appConfig.getAPI_URL(), HttpMethod.POST, rewardRequestEntity, PushServiceResponse.class);
                        httpResponseStop = Instant.now();
                        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
                        log.info("PUSH_API : Http Response Time Length {} ", timeElapsed.toMillis());
                        log.info("API RESPONSE {}, HEADERS {}", rewardResponse, rewardResponse.getHeaders());
                        if (rewardResponse.getStatusCode().is2xxSuccessful()) {
                            return ServiceResponse.builder().request(rewardType).response(rewardResponse.getBody()).build();
                        }
                        return null;

                    default:
                        log.error("ENDPOINT_TYPE does not match any of the given parameters! ENDPOINT_TYPE : {}", p.get(pushType.getValue()));
                        return null;
                }
            }
        }
        return null;
    }

    public SearchCustomerResponse callSearchCustomerService(String query, String queryType) throws Exception {
        URIBuilder builder = new URIBuilder(searchCustomerAppConfig.getAPI_URL());
        String newQuery = checkQueryType(queryType, query);
        builder.setParameter("q", newQuery).setParameter("t", queryType);
        log.info("API {} ", builder.build());
        log.info("QUERY {}, QUERY_TYPE {} ", query, queryType);
        HttpEntity<Void> headers = new HttpEntity<>(getHeaders());
        log.info("HEADERS {}", headers.getHeaders());
        Instant httpResponseStart = Instant.now();
        ResponseEntity<String> searchCustomerResponse = restTemplate.exchange(builder.build(), HttpMethod.GET, headers, String.class);
        Instant httpResponseStop = Instant.now();
        timeElapsed = Duration.between(httpResponseStart, httpResponseStop);
        log.info("SEARCH_CUSTOMER_API : Http Response Time Length {} ", timeElapsed.toMillis());
        log.info("SEARCH_CUSTOMER_RESPONSE {}", searchCustomerResponse);
        List<SearchCustomerResponse> searchCustomerResponses = Arrays.asList(objectMapper.readValue(searchCustomerResponse.getBody(), SearchCustomerResponse[].class));
            if (searchCustomerResponse.getStatusCode().is2xxSuccessful() && searchCustomerResponses.size() > 0) {
                SearchCustomerResponse customerResponse = searchCustomerResponses.get(searchCustomerResponses.size() - 1);
                log.info("USER_ID {}", customerResponse.getUserId());
                return customerResponse;
            }
        return null;
    }

    public HttpHeaders getHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Ocp-Apim-Subscription-Key", searchCustomerAppConfig.getOCP_APIM_SUBSCRIPTION_KEY());
        headers.add("Ocp-Apim-Trace", searchCustomerAppConfig.getOCP_APIM_TRACE());
        return headers;
    }

    public String checkQueryType(String queryType, String query) {
        if(query.startsWith("0") && queryType.equals("p")){
            return query;
        }
        return queryType.equals("p") ? "0" + query : query;
    }


    /*
    public static boolean isTimeBetween(String startTime, String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        LocalTime now = LocalTime.now();

        return start.isBefore(now) && end.isAfter(now);
    }

     */
}

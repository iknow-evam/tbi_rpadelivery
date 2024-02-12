package com.evam.marketing.communication.template.service.client;

import com.evam.marketing.communication.template.configuration.WebClientConfig;
import com.evam.marketing.communication.template.repository.status.model.StatusType;
import com.evam.marketing.communication.template.service.client.model.PushNotificationLog;
import com.evam.marketing.communication.template.service.client.model.PushType;
import com.evam.marketing.communication.template.service.client.model.request.abstracts.DataType;
import com.evam.marketing.communication.template.service.client.model.request.concretes.*;
import com.evam.marketing.communication.template.service.client.model.response.ErrorResponse;
import com.evam.marketing.communication.template.service.client.model.response.PushServiceResponse;
import com.evam.marketing.communication.template.service.client.model.response.SearchCustomerResponse;
import com.evam.marketing.communication.template.service.client.repository.LogRepository;
import com.evam.marketing.communication.template.service.event.KafkaProducerService;
import com.evam.marketing.communication.template.service.integration.model.request.CommunicationRequest;
import com.evam.marketing.communication.template.service.integration.model.response.CommunicationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@Slf4j
public class WorkerService extends AbstractCommunicationClient {
    private final WebClientConfig webClientConfig;
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

    private static final String QUERY = "QUERY";
    private static final String QUERY_TYPE = "QUERY_TYPE";
    private static final String USER_ID = "USER_ID";
    private final LogRepository logRepository;

    private final ObjectMapper objectMapper;

    private final PushNotificationLog.PushNotificationLogBuilder pushLog = PushNotificationLog.builder();


    public WorkerService(KafkaProducerService kafkaProducerService,
                         WebClientConfig webClientConfig,
                         LogRepository logRepository,
                         ObjectMapper objectMapper) {
        super(kafkaProducerService);
        this.webClientConfig = webClientConfig;
        this.logRepository = logRepository;
        this.objectMapper = objectMapper;
    }


    @RateLimiter(name = "client-limiter")
    public void callPushService(CommunicationRequest communicationRequest, Map<String, String> keyValueParam) {
        log.info("identifying use case for the payload ({})", keyValueParam);
        String campaignName = communicationRequest.getScenario();
        String offerId = communicationRequest.getCommunicationCode();

        try {
            String userId = keyValueParam.get(USER_ID);
            if (!userId.isEmpty()) {
                BaseNotificationRequest baseNotificationRequest = createRequestBody(keyValueParam, userId);
                if (baseNotificationRequest != null) {
                    webClientConfig.webClient().post()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromValue(baseNotificationRequest))
                            .retrieve()
                            .onStatus(
                                    HttpStatus::is4xxClientError,
                                    WorkerService::handleClientError
                            )
                            .onStatus(
                                    HttpStatus::is5xxServerError,
                                    WorkerService::handleClientError
                            )
                            .bodyToMono(PushServiceResponse.class)
                            //.retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(4)))
                            .subscribe(serviceResponse1 -> {
                                log.info("response after hitting endpoint is , RESPONSE {}", serviceResponse1);
                                CommunicationResponse communicationSuccessResponse = generateSuccessCommunicationResponse(
                                        communicationRequest, "Push Response", serviceResponse1.getNotificationId());
                                sendEvent(communicationSuccessResponse.toEvent());

                                pushLog.campaignName(campaignName)
                                        .userId(userId)
                                        .endpointType(keyValueParam.get(DATA_TYPE))
                                        .communicationCode(offerId)
                                        .response(serviceResponse1.getNotificationId())
                                        .request(baseNotificationRequest.toString())
                                        .communicationUUID(communicationRequest.getCommunicationUUID())
                                        .actorId(communicationRequest.getActorId())
                                        .status(StatusType.SENT.name());
                                this.logRepository.save(pushLog.build());

                            }, throwable -> {
                                log.error("Push Error occurred for actor [{}]. Error message: {}", communicationRequest.getActorId(), throwable.getMessage());
                                if (throwable instanceof ErrorResponse) {
                                    ErrorResponse exception = (ErrorResponse) throwable;
                                    log.info("RESPONSE {}, STATUS_CODE {}", exception.getMessage(), exception.getCode());
                                    CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                                            communicationRequest, exception.getMessage().replaceAll("\"", ""), exception.getCode());
                                    sendEvent(communicationResponse.toEvent());
                                    pushLog.campaignName(campaignName)
                                            .userId(userId)
                                            .endpointType(keyValueParam.get(DATA_TYPE))
                                            .communicationCode(offerId)
                                            .request(baseNotificationRequest.toString())
                                            .response(exception.getMessage().replaceAll("\"", ""))
                                            .communicationUUID(communicationRequest.getCommunicationUUID())
                                            .actorId(communicationRequest.getActorId())
                                            .status(StatusType.FAILED.name());
                                    this.logRepository.save(pushLog.build());
                                } else {
                                    CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                                            communicationRequest, "An unexpected error occurred", throwable.getMessage());
                                    sendEvent(communicationResponse.toEvent());
                                    pushLog.campaignName(campaignName)
                                            .userId(userId)
                                            .endpointType(keyValueParam.get(DATA_TYPE))
                                            .communicationCode(offerId)
                                            .request(baseNotificationRequest.toString())
                                            .response(throwable.getMessage())
                                            .communicationUUID(communicationRequest.getCommunicationUUID())
                                            .actorId(communicationRequest.getActorId())
                                            .status(StatusType.FAILED.name());
                                    this.logRepository.save(pushLog.build());
                                }

                            });
                }
            } else {
                CommunicationResponse communicationResponse = generateFailCommunicationResponse(
                        communicationRequest, "There is no User Id", "User Id Not Found");
                sendEvent(communicationResponse.toEvent());

                pushLog.campaignName(campaignName)
                        .endpointType(keyValueParam.get(DATA_TYPE))
                        .communicationCode(offerId)
                        .response("User Id Not Found")
                        .communicationUUID(communicationRequest.getCommunicationUUID())
                        .actorId(communicationRequest.getActorId())
                        .status(StatusType.FAILED.name());
                this.logRepository.save(pushLog.build());
            }

        } catch (Exception e) {
            log.error("An unexpected error occurred. Request: {}", communicationRequest, e);
            CommunicationResponse communicationExceptionResponse = generateFailCommunicationResponse(
                    communicationRequest, e.getMessage(), "UNEXPECTED");
            sendEvent(communicationExceptionResponse.toEvent());
            pushLog.campaignName(campaignName)
                    .endpointType(keyValueParam.get(DATA_TYPE))
                    .communicationCode(offerId)
                    .response(e.getMessage())
                    .request(communicationRequest.toString())
                    .communicationUUID(communicationRequest.getCommunicationUUID())
                    .actorId(communicationRequest.getActorId())
                    .status(StatusType.FAILED.name());
            this.logRepository.save(pushLog.build());
        }
    }


    public BaseNotificationRequest createRequestBody(Map<String, String> p, String userId) {
        try {
            if (p.containsKey(DATA_TYPE)) {
                String dataTypeStr = p.get(DATA_TYPE);
                log.debug(dataTypeStr);
                PushType pushType = PushType.fromString(dataTypeStr);
                DataType offerRequest;
                NotificationRequest.NotificationRequestBuilder notificationRequest = NotificationRequest.builder();
                notificationRequest.body(p.get(BODY));
                notificationRequest.title(p.get(TITLE));

                BaseNotificationRequest baseNotificationRequest = BaseNotificationRequest.builder()
                        .key(userId)
                        .keyType(p.get(KEY_TYPE))
                        .notificationType(p.get(NOTIFICATION_TYPE))
                        .notification(notificationRequest.build())
                        .build();
                if (!(pushType == null)) {

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
                            offerRequest = OfferRequest.builder().type(pushType.getValue()).build();
                            notificationRequest.data(offerRequest);
                            baseNotificationRequest.setNotification(notificationRequest.build());
                            break;
                        case LINK_PUSH:
                            offerRequest = LinkRequest.builder().type(pushType.getValue()).link(p.get(LINK)).badgeCount(p.get(BADGE_COUNT)).build();
                            notificationRequest.data(offerRequest);
                            baseNotificationRequest.setNotification(notificationRequest.build());
                            break;
                        case REPAYMENT_PUSH:
                            offerRequest = RepaymentRequest.builder().type(pushType.getValue()).campaignId(p.get(CAMPAIGN_ID)).description(p.get(DESCRIPTION)).badgeCount(p.get(BADGE_COUNT)).build();
                            notificationRequest.data(offerRequest);
                            baseNotificationRequest.setNotification(notificationRequest.build());
                            break;
                        case DEPOSIT:
                            offerRequest = DepositRequest.builder().type(pushType.getValue()).status(p.get(STATUS)).build();
                            notificationRequest.data(offerRequest);
                            baseNotificationRequest.setNotification(notificationRequest.build());
                            break;
                        case BNPL_OFFER:
                            offerRequest = BNPLOfferRequest.builder().type(pushType.getValue()).transactionId(p.get(TRANSACTION_ID)).badgeCount(p.get(BADGE_COUNT)).build();
                            notificationRequest.data(offerRequest);
                            baseNotificationRequest.setNotification(notificationRequest.build());
                            break;
                        case DEBIT_CARD:
                            offerRequest = DebitCardRequest.builder().type(pushType.getValue()).badgeCount(p.get(BADGE_COUNT)).build();
                            notificationRequest.data(offerRequest);
                            baseNotificationRequest.setNotification(notificationRequest.build());
                            break;
                        case REWARD:
                            offerRequest = RewardRequest.builder().type(pushType.getValue()).rewardStatus(p.get(REWARD_STATUS)).build();
                            notificationRequest.data(offerRequest);
                            baseNotificationRequest.setNotification(notificationRequest.build());
                            break;
                        default:
                            log.error("ENDPOINT_TYPE does not match any of the given parameters! ENDPOINT_TYPE : {}", p.get(pushType.getValue()));
                            return null;

                    }
                }
                log.info("PUSH_NOTIFICATION REQUEST {}", baseNotificationRequest);
                return baseNotificationRequest;
            }
        } catch (Exception e) {
            log.error("An unexpected exception occurred: {}", e.getMessage());
        }
        return null;

    }


    /*
    public Mono<String> callSearchCustomerService(String query, String queryType) {
        log.info("QUERY {}, QUERY_TYPE {} ", query, queryType);
        return webClientConfig.searchCustomerWebClient().get()
                .uri(uriBuilder -> uriBuilder.queryParam("q", query).queryParam("t", queryType).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatus::is4xxClientError,
                        WorkerService::handleClientError
                )
                .onStatus(
                        HttpStatus::is5xxServerError,
                        WorkerService::handleClientError
                )
                .bodyToMono(String.class)
                .mapNotNull(response -> {
                    log.info("SEARCH_CUSTOMER_RESPONSE {}", response);
                    if (response.equalsIgnoreCase("[]") || response.isEmpty()) {
                        log.info("USER_ID NOT FOUND");
                        return "";
                    } else {
                        try {
                            List<SearchCustomerResponse> searchCustomerResponses = Arrays.asList(objectMapper.readValue(response, SearchCustomerResponse[].class));
                            SearchCustomerResponse customerResponse = searchCustomerResponses.get(searchCustomerResponses.size() - 1);
                            log.info("USER_ID {}", customerResponse.getUserId());
                            return customerResponse.getUserId();
                        } catch (JsonProcessingException e) {
                            log.error("SEARCH_CUSTOMER_RESPONSE Parsing Error {}", e.getMessage());
                            return null;
                        }
                    }
                });
    }

     */

    private static Mono<? extends Throwable> handleClientError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> {
                    log.info("Error Body: " + body);
                    return Mono.error(new ErrorResponse(body, String.valueOf(response.statusCode().value())));
                });
    }


    @Override
    public void send(CommunicationRequest communicationRequest) {
    }

}

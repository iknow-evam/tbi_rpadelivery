package com.evam.marketing.communication.template.service.client.ex;

import lombok.Getter;

@Getter
public class RetryException extends RuntimeException{
    private final String responseBody;
    private int statusCode;


    public RetryException(String message, String responseBody, int value) {
        super(message);
        this.responseBody = responseBody;
        this.statusCode = value;
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
        this.responseBody = null;
    }

    public RetryException(Throwable cause) {
        super(cause);
        this.responseBody = null;
    }

}
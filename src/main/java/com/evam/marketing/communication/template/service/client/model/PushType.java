package com.evam.marketing.communication.template.service.client.model;

public enum PushType {
    LINK_PUSH("link"),
    REPAYMENT_PUSH("repayment"),
    OFFER_PUSH("offer"),

    SELF_ID("selfId"),

    PAYMENT("payment"),

    LOAN_STATUS("loanStatus"),

    CONSTANT_SCORING_FINISHED("constantScoringFinished"),

    CONSTANT_SCORING("constantScoring"),

    DEBIT_CARD("debitCard"),

    REPAYMENT("repayment"),

    REGULAR_LOAN("regularLoan"),

    BNPL_OFFER("BNPLOffer"),
    DEPOSIT("deposit"),
    DEPOSIT_OPENING("depositOpening"),
    BNPL_CONTRACT_SIGN("BNPLContractSign"),

    FINANCIAL_OPERATIONS("financialOperations"),
    NON_FINANCIAL_OPERATIONS("nonFinancialOperations"),
    KASICHKA("kasichka"),
    CDL_LIMIT_OFFER("cdlLimitOffer"),
    UTILITY_PAYMENT("utilityPayment"),
    OTHER("other"),
    REWARD("reward");

    private final String value;

    PushType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PushType fromString(String str) {
        for (PushType dataType : values()) {
            if (dataType.getValue().equalsIgnoreCase(str)) {
                return dataType;
            }
        }
        return null;
    }
}

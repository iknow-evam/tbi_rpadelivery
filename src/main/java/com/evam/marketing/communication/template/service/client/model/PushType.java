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

    BNPL_CONTRACT_SIGN("BNPLContractSign");
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

package com.poslink.sample.report.model;

/**
 * Created by Leon.F on 2018/4/10.
 */

public class ReportRequestEntity {
    private String transType;
    private String cardType;
    private String edcType;

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getEdcType() {
        return edcType;
    }

    public void setEdcType(String edcType) {
        this.edcType = edcType;
    }
}

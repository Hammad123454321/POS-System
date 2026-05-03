package com.pax.poslink.dal.print;

import java.io.Serializable;

/**
 * Created by Leon on 2017/12/6.
 */

public class OtherInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String transNO;
    private String authCode;
    private String href;
    private String cardNO;
    private String expDate;

    public OtherInfoEntity(String transNO, String authCode, String href, String cardNO, String expDate) {
        this.transNO = transNO;
        this.authCode = authCode;
        this.href = href;
        this.cardNO = cardNO;
        this.expDate = expDate;
    }

    public String getTransNO() {
        return transNO;
    }

    public String getAuthCode() {
        return authCode;
    }

    public String getHref() {
        return href;
    }

    public String getCardNO() {
        return cardNO;
    }

    public String getExpDate() {
        return expDate;
    }
}

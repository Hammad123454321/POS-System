package com.pax.poslink.fullIntegration.mock;

import com.pax.poslink.base.BaseRequest;

/**
 * Created by Leon.F on 2018/5/24.
 */
public class MockRequest extends BaseRequest<String> {


    private String EMVData;
    private String host;
    private int port;
    private String amount;
    private String path;
    private String expDate;
    private String track2Data;
    private String APIKey;

    @Override
    public String pack() {
        XmlObjectDom4j rootObj = new XmlObjectDom4j("transaction");
        rootObj.put("type", "SALE");
        rootObj.put("exp-date", expDate);
        rootObj.put("track2-data", track2Data);
        rootObj.put("track-format", "TRACK2");
        rootObj.put("amount", amount);
        rootObj.put("emv-data", EMVData);
        rootObj.put("emv-entry-method", "EMV_DIP");
        rootObj.put("cvm", "MSG");
        XmlObjectDom4j posCap = new XmlObjectDom4j("pos-capabilities");
        posCap.put("pos-capability", "EMV");
        posCap.put("pos-capability", "MAGSTRIPE");
        rootObj.put("pos-capabilities", posCap);
        return rootObj.toString();
    }

    public void setEMVData(String EMVData) {
        this.EMVData = EMVData;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getEMVData() {
        return EMVData;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getAmount() {
        return amount;
    }

    public String getPath() {
        return path;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
    }

    public String getAPIKey() {
        return APIKey;
    }
}

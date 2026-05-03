package com.poslink.sample.payment.model;

import com.poslink.sample.common.model.BaseResponse;

/**
 * Created by Leon.F on 2018/2/7.
 */

public class PaymentResponseEntity extends BaseResponse {

    private Data data = new Data();

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }


    public static class Data {

        private String resultCode = "";
        private String resultTxt = "";
        private String authCode = "";
        private String approvedAmount = "";
        private String avsResponse = "";
        private String bogusAccountNum = "";
        private String cardType = "";
        private String cvResponse = "";
        private String hostCode = "";
        private String hostResponse = "";
        private String message = "";
        private String refNum = "";
        private String rawResponse = "";
        private String remainingBalance = "";
        private String extraBalance = "";
        private String requestedAmount = "";
        private String timestamp = "";
        private String sigFileName = "";
        private String signData = "";
        private String extData = "";

        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public void setResultTxt(String resultTxt) {
            this.resultTxt = resultTxt;
        }

        public String getResultTxt() {
            return resultTxt;
        }

        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
        }

        public String getApprovedAmount() {
            return approvedAmount;
        }

        public void setApprovedAmount(String approvedAmount) {
            this.approvedAmount = approvedAmount;
        }

        public String getAvsResponse() {
            return avsResponse;
        }

        public void setAvsResponse(String avsResponse) {
            this.avsResponse = avsResponse;
        }

        public String getBogusAccountNum() {
            return bogusAccountNum;
        }

        public void setBogusAccountNum(String bogusAccountNum) {
            this.bogusAccountNum = bogusAccountNum;
        }

        public String getCardType() {
            return cardType;
        }

        public void setCardType(String cardType) {
            this.cardType = cardType;
        }

        public String getCvResponse() {
            return cvResponse;
        }

        public void setCvResponse(String cvResponse) {
            this.cvResponse = cvResponse;
        }

        public String getHostCode() {
            return hostCode;
        }

        public void setHostCode(String hostCode) {
            this.hostCode = hostCode;
        }

        public String getHostResponse() {
            return hostResponse;
        }

        public void setHostResponse(String hostResponse) {
            this.hostResponse = hostResponse;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getRefNum() {
            return refNum;
        }

        public void setRefNum(String refNum) {
            this.refNum = refNum;
        }

        public String getRawResponse() {
            return rawResponse;
        }

        public void setRawResponse(String rawResponse) {
            this.rawResponse = rawResponse;
        }

        public String getRemainingBalance() {
            return remainingBalance;
        }

        public void setRemainingBalance(String remainingBalance) {
            this.remainingBalance = remainingBalance;
        }

        public String getExtraBalance() {
            return extraBalance;
        }

        public void setExtraBalance(String extraBalance) {
            this.extraBalance = extraBalance;
        }

        public String getRequestedAmount() {
            return requestedAmount;
        }

        public void setRequestedAmount(String requestedAmount) {
            this.requestedAmount = requestedAmount;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getSigFileName() {
            return sigFileName;
        }

        public void setSigFileName(String sigFileName) {
            this.sigFileName = sigFileName;
        }

        public String getSignData() {
            return signData;
        }

        public void setSignData(String signData) {
            this.signData = signData;
        }

        public String getExtData() {
            return extData;
        }

        public void setExtData(String extData) {
            this.extData = extData;
        }
    }
}

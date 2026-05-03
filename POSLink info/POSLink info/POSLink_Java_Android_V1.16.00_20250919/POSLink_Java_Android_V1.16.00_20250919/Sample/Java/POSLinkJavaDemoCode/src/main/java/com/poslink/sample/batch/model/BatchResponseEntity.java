package com.poslink.sample.batch.model;

import com.google.gson.annotations.SerializedName;
import com.poslink.sample.common.model.BaseResponse;

/**
 * Created by Leon.F on 2018/2/7.
 */

public class BatchResponseEntity extends BaseResponse {

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
        private String creditCount = "";
        private String creditAmount = "";
        private String debitCount = "";
        private String debitAmount = "";

        @SerializedName("ebtCount")
        private String ebtCount = "";
        @SerializedName("ebtAmount")
        private String ebtAmount = "";
        private String giftCount = "";
        private String giftAmount = "";
        private String loyaltyCount = "";
        private String loyaltyAmount = "";
        private String cashCount = "";
        private String cashAmount = "";
        private String checkCount = "";
        private String checkAmount = "";
        private String timestamp = "";
        @SerializedName("tid")
        private String tid = "";
        @SerializedName("mid")
        private String mid = "";
        private String hostTraceNum = "";
        private String batchNum = "";
        private String authCode = "";
        private String hostCode = "";
        private String hostResponse = "";
        private String message = "";
        private String extData = "";
        @SerializedName("safTotalCount")
        private String safTotalCount = "";
        @SerializedName("safTotalAmount")
        private String safTotalAmount = "";
        @SerializedName("safUploadedCount")
        private String safUploadedCount = "";
        @SerializedName("safUploadedAmount")
        private String safUploadedAmount = "";
        @SerializedName("safFailedCount")
        private String safFailedCount = "";
        @SerializedName("safFailedTotal")
        private String safFailedTotal = "";
        @SerializedName("safDeletedCount")
        private String safDeletedCount = "";
        private String batchFailedRefNum = "";
        private String batchFailedCount = "";

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

        public String getCreditCount() {
            return creditCount;
        }

        public void setCreditCount(String creditCount) {
            this.creditCount = creditCount;
        }

        public String getCreditAmount() {
            return creditAmount;
        }

        public void setCreditAmount(String creditAmount) {
            this.creditAmount = creditAmount;
        }

        public String getDebitCount() {
            return debitCount;
        }

        public void setDebitCount(String debitCount) {
            this.debitCount = debitCount;
        }

        public String getDebitAmount() {
            return debitAmount;
        }

        public void setDebitAmount(String debitAmount) {
            this.debitAmount = debitAmount;
        }

        public String getEBTCount() {
            return ebtCount;
        }

        public void setEBTCount(String eBTCount) {
            this.ebtCount = eBTCount;
        }

        public String getEBTAmount() {
            return ebtAmount;
        }

        public void setEBTAmount(String eBTAmount) {
            this.ebtAmount = eBTAmount;
        }

        public String getGiftCount() {
            return giftCount;
        }

        public void setGiftCount(String giftCount) {
            this.giftCount = giftCount;
        }

        public String getGiftAmount() {
            return giftAmount;
        }

        public void setGiftAmount(String giftAmount) {
            this.giftAmount = giftAmount;
        }

        public String getLoyaltyCount() {
            return loyaltyCount;
        }

        public void setLoyaltyCount(String loyaltyCount) {
            this.loyaltyCount = loyaltyCount;
        }

        public String getLoyaltyAmount() {
            return loyaltyAmount;
        }

        public void setLoyaltyAmount(String loyaltyAmount) {
            this.loyaltyAmount = loyaltyAmount;
        }

        public String getCashCount() {
            return cashCount;
        }

        public void setCashCount(String cashCount) {
            this.cashCount = cashCount;
        }

        public String getCashAmount() {
            return cashAmount;
        }

        public void setCashAmount(String cashAmount) {
            this.cashAmount = cashAmount;
        }

        public String getCheckCount() {
            return checkCount;
        }

        public void setCheckCount(String checkCount) {
            this.checkCount = checkCount;
        }

        public String getCheckAmount() {
            return checkAmount;
        }

        public void setCheckAmount(String checkAmount) {
            this.checkAmount = checkAmount;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getTID() {
            return tid;
        }

        public void setTID(String TID) {
            this.tid = TID;
        }

        public String getMID() {
            return mid;
        }

        public void setMID(String MID) {
            this.mid = MID;
        }

        public String getHostTraceNum() {
            return hostTraceNum;
        }

        public void setHostTraceNum(String hostTraceNum) {
            this.hostTraceNum = hostTraceNum;
        }

        public String getBatchNum() {
            return batchNum;
        }

        public void setBatchNum(String batchNum) {
            this.batchNum = batchNum;
        }

        public String getAuthCode() {
            return authCode;
        }

        public void setAuthCode(String authCode) {
            this.authCode = authCode;
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

        public String getExtData() {
            return extData;
        }

        public void setExtData(String extData) {
            this.extData = extData;
        }

        public String getSAFTotalCount() {
            return safTotalCount;
        }

        public void setSAFTotalCount(String SAFTotalCount) {
            this.safTotalCount = SAFTotalCount;
        }

        public String getSAFTotalAmount() {
            return safTotalAmount;
        }

        public void setSAFTotalAmount(String SAFTotalAmount) {
            this.safTotalAmount = SAFTotalAmount;
        }

        public String getSAFUploadedCount() {
            return safUploadedCount;
        }

        public void setSAFUploadedCount(String SAFUploadedCount) {
            this.safUploadedCount = SAFUploadedCount;
        }

        public String getSAFUploadedAmount() {
            return safUploadedAmount;
        }

        public void setSAFUploadedAmount(String SAFUploadedAmount) {
            this.safUploadedAmount = SAFUploadedAmount;
        }

        public String getSAFFailedCount() {
            return safFailedCount;
        }

        public void setSAFFailedCount(String SAFFailedCount) {
            this.safFailedCount = SAFFailedCount;
        }

        public String getSAFFailedTotal() {
            return safFailedTotal;
        }

        public void setSAFFailedTotal(String SAFFailedTotal) {
            this.safFailedTotal = SAFFailedTotal;
        }

        public String getSAFDeletedCount() {
            return safDeletedCount;
        }

        public void setSAFDeletedCount(String SAFDeletedCount) {
            this.safDeletedCount = SAFDeletedCount;
        }

        public String getBatchFailedRefNum() {
            return batchFailedRefNum;
        }

        public void setBatchFailedRefNum(String batchFailedRefNum) {
            this.batchFailedRefNum = batchFailedRefNum;
        }

        public String getBatchFailedCount() {
            return batchFailedCount;
        }

        public void setBatchFailedCount(String batchFailedCount) {
            this.batchFailedCount = batchFailedCount;
        }
    }
}

package com.poslink.sample.report.model;

import com.google.gson.annotations.SerializedName;
import com.poslink.sample.common.model.BaseResponse;

/**
 * Created by Leon.F on 2018/4/10.
 */

public class ReportResponseEntity extends BaseResponse {

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
        private String EDCType="";
        private String creditCount ="";
        private String creditAmount ="";

        private String debitCount ="";

        private String debitAmount ="";

        @SerializedName("ebtCount")
        private String EBTCount="";

        @SerializedName("ebtAmount")
        private String EBTAmount="";

        private String giftCount ="";

        private String giftAmount ="";

        private String loyaltyCount ="";

        private String loyaltyAmount ="";

        private String cashCount ="";

        private String cashAmount ="";

        private String checkCount ="";

        private String checkAmount ="";

        private String extData ="";
        private String transTotal ="";

        public String getResultCode() {
            return resultCode;
        }

        public void setResultCode(String resultCode) {
            this.resultCode = resultCode;
        }

        public String getResultTxt() {
            return resultTxt;
        }

        public void setResultTxt(String resultTxt) {
            this.resultTxt = resultTxt;
        }

        public String getEDCType() {
            return EDCType;
        }

        public void setEDCType(String EDCType) {
            this.EDCType = EDCType;
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
            return EBTCount;
        }

        public void setEBTCount(String EBTCount) {
            this.EBTCount = EBTCount;
        }

        public String getEBTAmount() {
            return EBTAmount;
        }

        public void setEBTAmount(String EBTAmount) {
            this.EBTAmount = EBTAmount;
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

        public String getExtData() {
            return extData;
        }

        public void setExtData(String extData) {
            this.extData = extData;
        }

        public String getTransTotal() {
            return transTotal;
        }

        public void setTransTotal(String transTotal) {
            this.transTotal = transTotal;
        }
    }
}

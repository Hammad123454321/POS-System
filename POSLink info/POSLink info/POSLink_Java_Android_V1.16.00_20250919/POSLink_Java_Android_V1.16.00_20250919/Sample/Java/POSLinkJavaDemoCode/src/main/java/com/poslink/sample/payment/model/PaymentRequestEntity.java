package com.poslink.sample.payment.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Leon.F on 2018/2/7.
 */

public class PaymentRequestEntity {
    private String edcType;
    private String transType;
    private String amount;
    private String cashBackAmt;
    private String clerkID = "";
    private String zip = "";
    private String tipAmt = "";
    private String taxAmt = "";
    private String street = "";
    private String street2 = "";
    private String surchargeAmt = "";
    private String serverID = "";
    private String autoSubmit = "";
    private String PONum = "";
    private String origRefNum = "";
    private String misc3Amt = "";
    private String misc2Amt = "";
    private String misc1Amt = "";
    private String merchantKey = "";
    private String invNum = "";
    @SerializedName("ECRRefNum")
    private String ecrRefNum = "";
    @SerializedName("ECRTransID")
    private String ECRTransID = "";
    private String origECRRefNum = "";
    private String authCode = "";
    private String extData = "";
    private String fuelAmt = "";

    public PaymentRequestEntity() {
    }

    public String getEdcType() {
        return edcType;
    }

    public void setEdcType(String edcType) {
        this.edcType = edcType;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCashBackAmt() {
        return cashBackAmt;
    }

    public void setCashBackAmt(String cashBackAmt) {
        this.cashBackAmt = cashBackAmt;
    }

    public String getClerkID() {
        return clerkID;
    }

    public void setClerkID(String clerkID) {
        this.clerkID = clerkID;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getTipAmt() {
        return tipAmt;
    }

    public void setTipAmt(String tipAmt) {
        this.tipAmt = tipAmt;
    }

    public String getTaxAmt() {
        return taxAmt;
    }

    public void setTaxAmt(String taxAmt) {
        this.taxAmt = taxAmt;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getStreet2() {
        return street2;
    }

    public void setStreet2(String street2) {
        this.street2 = street2;
    }

    public String getSurchargeAmt() {
        return surchargeAmt;
    }

    public void setSurchargeAmt(String surchargeAmt) {
        this.surchargeAmt = surchargeAmt;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getAutoSubmit() {
        return autoSubmit;
    }

    public void setAutoSubmit(String autoSubmit) {
        this.autoSubmit = autoSubmit;
    }

    public String getPONum() {
        return PONum;
    }

    public void setPONum(String PONum) {
        this.PONum = PONum;
    }

    public String getOrigRefNum() {
        return origRefNum;
    }

    public void setOrigRefNum(String origRefNum) {
        this.origRefNum = origRefNum;
    }

    public String getMisc3Amt() {
        return misc3Amt;
    }

    public void setMisc3Amt(String misc3Amt) {
        this.misc3Amt = misc3Amt;
    }

    public String getMisc2Amt() {
        return misc2Amt;
    }

    public void setMisc2Amt(String misc2Amt) {
        this.misc2Amt = misc2Amt;
    }

    public String getMisc1Amt() {
        return misc1Amt;
    }

    public void setMisc1Amt(String misc1Amt) {
        this.misc1Amt = misc1Amt;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public void setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
    }

    public String getInvNum() {
        return invNum;
    }

    public void setInvNum(String invNum) {
        this.invNum = invNum;
    }

    public String getECRRefNum() {
        return ecrRefNum;
    }

    public void setECRRefNum(String eCRRefNum) {
        this.ecrRefNum = eCRRefNum;
    }

    public String getECRTransID() {
        return ECRTransID;
    }

    public void setECRTransID(String ecrTransid) {
        this.ECRTransID = ecrTransid;
    }

    public String getOrigECRRefNum() {
        return origECRRefNum;
    }

    public void setOrigECRRefNum(String origECRRefNum) {
        this.origECRRefNum = origECRRefNum;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getExtData() {
        return extData;
    }

    public void setExtData(String extData) {
        this.extData = extData;
    }

    public String getFuelAmt() {
        return fuelAmt;
    }

    public void setFuelAmt(String fuelAmt) {
        this.fuelAmt = fuelAmt;
    }
}

package com.poslink.sample.manage.model;

import com.google.gson.annotations.SerializedName;
import com.poslink.sample.common.model.BaseResponse;

/**
 * Created by Leon.F on 2018/3/9.
 */

public class ManageResponseEntity extends BaseResponse {

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
        private String varValue = "";
        private String varValue1 = "";
        private String varValue2 = "";
        private String varValue3 = "";
        private String varValue4 = "";
        private String buttonNum = "";
        private String sigFileName = "";
        private String pinBlock = "";
        private String ksn = "";
        private String entryMode = "";
        private String track1Data = "";
        private String track2Data = "";
        private String track3Data = "";
        private String pan = "";
        private String expiryDate = "";
        @SerializedName("qrCode")
        private String qrCode = "";
        private String text = "";
        @SerializedName("emvData")
        private String emvData = "";
        private String extData = "";
        private String authorizationResult = "";
        private String signatureFlag = "";
        private String tagList = "";
        private String modelName = "";
        private String primaryFirmVersion = "";
        private String macAddress = "";
        private String linesPerScreen = "";
        private String charsPerLine = "";
        private String status = "";
        private String sn = "";

        private String maskedPAN = "";
        private String barcodeType = "";
        private String barcodeData = "";
        private String encryptionTransmissionBlock;
        private String contactlessTransactionPath = "";
        private String onlinePINFlag = "";
        private String encryptedEMVTLVData = "";
        private String encryptedSensitiveTLVData = "";
        private String cardholder = "";
        private String serviceCode = "";
        private String cvvCode = "";
        private String zipCode = "";
        private String vasCode = "";
        private String[] vasData;
        private String NDEFData = "";

        private String pinBypassStatus = "";
        private String issuerScriptResults = "";
        private String luhnValidationResult = "";

        public String getMaskedPAN() {
            return this.maskedPAN;
        }

        public void setMaskedPAN(String maskedPAN) {
            this.maskedPAN = maskedPAN;
        }

        public String getBarcodeType() {
            return this.barcodeType;
        }

        public void setBarcodeType(String barcodeType) {
            this.barcodeType = barcodeType;
        }

        public String getBarcodeData() {
            return this.barcodeData;
        }

        public void setBarcodeData(String barcodeData) {
            this.barcodeData = barcodeData;
        }

        public String getEncryptionTransmissionBlock() {
            return this.encryptionTransmissionBlock;
        }

        public void setEncryptionTransmissionBlock(String encryptionTransmissionBlock) {
            this.encryptionTransmissionBlock = encryptionTransmissionBlock;
        }

        public String getContactlessTransactionPath() {
            return this.contactlessTransactionPath;
        }

        public void setContactlessTransactionPath(String contactlessTransactionPath) {
            this.contactlessTransactionPath = contactlessTransactionPath;
        }

        public String getOnlinePINFlag() {
            return this.onlinePINFlag;
        }

        public void setOnlinePINFlag(String onlinePINFlag) {
            this.onlinePINFlag = onlinePINFlag;
        }

        public String getEncryptedEMVTLVData() {
            return this.encryptedEMVTLVData;
        }

        public void setEncryptedEMVTLVData(String encryptedEMVTLVData) {
            this.encryptedEMVTLVData = encryptedEMVTLVData;
        }

        public String getEncryptedSensitiveTLVData() {
            return this.encryptedSensitiveTLVData;
        }

        public void setEncryptedSensitiveTLVData(String encryptedSensitiveTLVData) {
            this.encryptedSensitiveTLVData = encryptedSensitiveTLVData;
        }

        public String getCardholder() {
            return this.cardholder;
        }

        public void setCardholder(String cardholder) {
            this.cardholder = cardholder;
        }

        public String getServiceCode() {
            return this.serviceCode;
        }

        public void setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
        }

        public String getCvvCode() {
            return this.cvvCode;
        }

        public void setCvvCode(String cvvCode) {
            this.cvvCode = cvvCode;
        }

        public String getZipCode() {
            return this.zipCode;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public String getVasCode() {
            return this.vasCode;
        }

        public void setVasCode(String vasCode) {
            this.vasCode = vasCode;
        }

        public String[] getVasData() {
            return this.vasData;
        }

        public void setVasData(String[] vasData) {
            this.vasData = vasData;
        }

        public String getNDEFData() {
            return this.NDEFData;
        }

        public void setNDEFData(String NDEFData) {
            this.NDEFData = NDEFData;
        }

        public String getPinBypassStatus() {
            return this.pinBypassStatus;
        }

        public void setPinBypassStatus(String pinBypassStatus) {
            this.pinBypassStatus = pinBypassStatus;
        }

        public String getIssuerScriptResults() {
            return this.issuerScriptResults;
        }

        public void setIssuerScriptResults(String issuerScriptResults) {
            this.issuerScriptResults = issuerScriptResults;
        }

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

        public String getVarValue() {
            return varValue;
        }

        public void setVarValue(String varValue) {
            this.varValue = varValue;
        }

        public String getVarValue1() {
            return varValue1;
        }

        public void setVarValue1(String varValue1) {
            this.varValue1 = varValue1;
        }

        public String getVarValue2() {
            return varValue2;
        }

        public void setVarValue2(String varValue2) {
            this.varValue2 = varValue2;
        }

        public String getVarValue3() {
            return varValue3;
        }

        public void setVarValue3(String varValue3) {
            this.varValue3 = varValue3;
        }

        public String getVarValue4() {
            return varValue4;
        }

        public void setVarValue4(String varValue4) {
            this.varValue4 = varValue4;
        }

        public String getButtonNum() {
            return buttonNum;
        }

        public void setButtonNum(String buttonNum) {
            this.buttonNum = buttonNum;
        }

        public String getSigFileName() {
            return sigFileName;
        }

        public void setSigFileName(String sigFileName) {
            this.sigFileName = sigFileName;
        }

        public String getPinBlock() {
            return pinBlock;
        }

        public void setPinBlock(String pinBlock) {
            this.pinBlock = pinBlock;
        }

        public String getKsn() {
            return ksn;
        }

        public void setKsn(String ksn) {
            this.ksn = ksn;
        }

        public String getEntryMode() {
            return entryMode;
        }

        public void setEntryMode(String entryMode) {
            this.entryMode = entryMode;
        }

        public String getTrack1Data() {
            return track1Data;
        }

        public void setTrack1Data(String track1Data) {
            this.track1Data = track1Data;
        }

        public String getTrack2Data() {
            return track2Data;
        }

        public void setTrack2Data(String track2Data) {
            this.track2Data = track2Data;
        }

        public String getTrack3Data() {
            return track3Data;
        }

        public void setTrack3Data(String track3Data) {
            this.track3Data = track3Data;
        }

        public String getPan() {
            return pan;
        }

        public void setPan(String pan) {
            this.pan = pan;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }

        public String getQRCode() {
            return qrCode;
        }

        public void setQRCode(String QRCode) {
            this.qrCode = QRCode;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getEMVData() {
            return emvData;
        }

        public void setEMVData(String EMVData) {
            this.emvData = EMVData;
        }

        public String getExtData() {
            return extData;
        }

        public void setExtData(String extData) {
            this.extData = extData;
        }

        public String getAuthorizationResult() {
            return authorizationResult;
        }

        public void setAuthorizationResult(String authorizationResult) {
            this.authorizationResult = authorizationResult;
        }

        public String getSignatureFlag() {
            return signatureFlag;
        }

        public void setSignatureFlag(String signatureFlag) {
            this.signatureFlag = signatureFlag;
        }

        public String getTagList() {
            return tagList;
        }

        public void setTagList(String tagList) {
            this.tagList = tagList;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getPrimaryFirmVersion() {
            return primaryFirmVersion;
        }

        public void setPrimaryFirmVersion(String primaryFirmVersion) {
            this.primaryFirmVersion = primaryFirmVersion;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public String getLinesPerScreen() {
            return linesPerScreen;
        }

        public void setLinesPerScreen(String linesPerScreen) {
            this.linesPerScreen = linesPerScreen;
        }

        public String getCharsPerLine() {
            return charsPerLine;
        }

        public void setCharsPerLine(String charsPerLine) {
            this.charsPerLine = charsPerLine;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getLuhnValidationResult() {
            return luhnValidationResult;
        }

        public void setLuhnValidationResult(String luhnValidationResult) {
            this.luhnValidationResult = luhnValidationResult;
        }
    }
}

package com.poslink.sample.manage.model;


/**
 * Created by Leon.F on 2018/3/9.
 */

public class ManageRequestEntity {

    private String transType = "";
    private String edcType = "";
    private String trans = "";

    private String varName = "";
    private String varValue = "";
    private String varName1 = "";
    private String varValue1 = "";
    private String varName2 = "";
    private String varValue2 = "";
    private String varName3 = "";
    private String varValue3 = "";
    private String varName4 = "";
    private String varValue4 = "";

    private String title = "";
    private String button1 = "";
    private String button2 = "";
    private String button3 = "";
    private String button4 = "";
    private String displayMessage = "";
    private String displayMessage2 = "";
    private String imagePath = "";
    private String imageName = "";
    private String topDown = "";
    private String taxLine = "";
    private String totalLine = "";
    private String imageDescription = "";
    private String itemData = "";

    private String upload = "";
    private String hrefNum = "";
    private String timeOut = "";
    private String thankYouTitle = "";
    private String thankYouMessage1 = "";
    private String thankYouMessage2 = "";
    private String thankYouTimeOut = "";
    private String sigSavePath;

    private String amount = "";
    private String cashBackAmt = "";
    private String magneticSwipeEntryFlag = "";
    private String manualEntryFlag = "";
    private String contactlessEntryFlag = "";
    private String contactEMVEntryFlag = "";
    private String fallbackSwipeEntryFlag = "";
    private String expiryDatePrompt = "";
    private String cvvPrompt = "";
    private String zipCodePrompt = "";
    private String encryptionFlag = "";
    private String keySlot = "";
    private String paddingChar = "";
    private String trackDataSentinel = "";
    private String minAccountLength = "";
    private String maxAccountLength = "";
    private String emvKernelConfigurationSelection = "";
    private String transactionDate = "";
    private String transactionTime = "";
    private String currencyCode = "";
    private String currencyExponent = "";
    private String merchantCategoryCode = "";
    private String transactionSequenceNumber = "";
    private String tagList = "";

    private String merchantDecision = "";
    private String encryptionType = "";
    private String pinMinLength = "";
    private String pinMaxLength = "";
    private String pinBypass = "";
    private String pinAlgorithm = "";

    private String onlineAuthorizationResult = "";
    private String responseCode = "";
    private String authorizationCode = "";
    private String issuerAuthenticationData = "";
    private String issuerScript1 = "";
    private String issuerScript2 = "";

    private String accountNumber = "";
    private String nullPIN = "";
    private String filePath = "";
    private String fileType = "";

    private String targetDevice = "";
    public String getNullPIN() {
        return this.nullPIN;
    }

    public void setNullPIN(final String nullPIN) {
        this.nullPIN = nullPIN;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setEdcType(final String edcType) {
        this.edcType = edcType;
    }

    public String getCvvPrompt() {
        return this.cvvPrompt;
    }

    public void setCvvPrompt(final String cvvPrompt) {
        this.cvvPrompt = cvvPrompt;
    }

    public String getMerchantDecision() {
        return this.merchantDecision;
    }

    public void setMerchantDecision(final String merchantDecision) {
        this.merchantDecision = merchantDecision;
    }

    public String getEncryptionType() {
        return this.encryptionType;
    }

    public void setEncryptionType(final String encryptionType) {
        this.encryptionType = encryptionType;
    }

    public String getPinMinLength() {
        return this.pinMinLength;
    }

    public void setPinMinLength(final String pinMinLength) {
        this.pinMinLength = pinMinLength;
    }

    public String getPinMaxLength() {
        return this.pinMaxLength;
    }

    public void setPinMaxLength(final String pinMaxLength) {
        this.pinMaxLength = pinMaxLength;
    }

    public String getPinBypass() {
        return this.pinBypass;
    }

    public void setPinBypass(final String pinBypass) {
        this.pinBypass = pinBypass;
    }

    public String getPinAlgorithm() {
        return this.pinAlgorithm;
    }

    public void setPinAlgorithm(final String pinAlgorithm) {
        this.pinAlgorithm = pinAlgorithm;
    }

    public String getOnlineAuthorizationResult() {
        return this.onlineAuthorizationResult;
    }

    public void setOnlineAuthorizationResult(final String onlineAuthorizationResult) {
        this.onlineAuthorizationResult = onlineAuthorizationResult;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(final String responseCode) {
        this.responseCode = responseCode;
    }

    public String getAuthorizationCode() {
        return this.authorizationCode;
    }

    public void setAuthorizationCode(final String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getIssuerAuthenticationData() {
        return this.issuerAuthenticationData;
    }

    public void setIssuerAuthenticationData(final String issuerAuthenticationData) {
        this.issuerAuthenticationData = issuerAuthenticationData;
    }

    public String getIssuerScript1() {
        return this.issuerScript1;
    }

    public void setIssuerScript1(final String issuerScript1) {
        this.issuerScript1 = issuerScript1;
    }

    public String getIssuerScript2() {
        return this.issuerScript2;
    }

    public void setIssuerScript2(final String issuerScript2) {
        this.issuerScript2 = issuerScript2;
    }

    public String getAmount() {
        return this.amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getCashBackAmt() {
        return this.cashBackAmt;
    }

    public void setCashBackAmt(final String cashBackAmt) {
        this.cashBackAmt = cashBackAmt;
    }

    public String getMagneticSwipeEntryFlag() {
        return this.magneticSwipeEntryFlag;
    }

    public void setMagneticSwipeEntryFlag(final String magneticSwipeEntryFlag) {
        this.magneticSwipeEntryFlag = magneticSwipeEntryFlag;
    }

    public String getManualEntryFlag() {
        return this.manualEntryFlag;
    }

    public void setManualEntryFlag(final String manualEntryFlag) {
        this.manualEntryFlag = manualEntryFlag;
    }

    public String getContactlessEntryFlag() {
        return this.contactlessEntryFlag;
    }

    public void setContactlessEntryFlag(final String contactlessEntryFlag) {
        this.contactlessEntryFlag = contactlessEntryFlag;
    }

    public String getContactEMVEntryFlag() {
        return this.contactEMVEntryFlag;
    }

    public void setContactEMVEntryFlag(final String contactEMVEntryFlag) {
        this.contactEMVEntryFlag = contactEMVEntryFlag;
    }

    public String getFallbackSwipeEntryFlag() {
        return this.fallbackSwipeEntryFlag;
    }

    public void setFallbackSwipeEntryFlag(final String fallbackSwipeEntryFlag) {
        this.fallbackSwipeEntryFlag = fallbackSwipeEntryFlag;
    }

    public String getExpiryDatePrompt() {
        return this.expiryDatePrompt;
    }

    public void setExpiryDatePrompt(final String expiryDatePrompt) {
        this.expiryDatePrompt = expiryDatePrompt;
    }

    public String getcvvPrompt() {
        return this.cvvPrompt;
    }

    public void setcvvPrompt(final String cVVPrompt) {
        this.cvvPrompt = cVVPrompt;
    }

    public String getZipCodePrompt() {
        return this.zipCodePrompt;
    }

    public void setZipCodePrompt(final String zipCodePrompt) {
        this.zipCodePrompt = zipCodePrompt;
    }

    public String getEncryptionFlag() {
        return this.encryptionFlag;
    }

    public void setEncryptionFlag(final String encryptionFlag) {
        this.encryptionFlag = encryptionFlag;
    }

    public String getKeySlot() {
        return this.keySlot;
    }

    public void setKeySlot(final String keySlot) {
        this.keySlot = keySlot;
    }

    public String getPaddingChar() {
        return this.paddingChar;
    }

    public void setPaddingChar(final String paddingChar) {
        this.paddingChar = paddingChar;
    }

    public String getTrackDataSentinel() {
        return this.trackDataSentinel;
    }

    public void setTrackDataSentinel(final String trackDataSentinel) {
        this.trackDataSentinel = trackDataSentinel;
    }

    public String getMinAccountLength() {
        return this.minAccountLength;
    }

    public void setMinAccountLength(final String minAccountLength) {
        this.minAccountLength = minAccountLength;
    }

    public String getMaxAccountLength() {
        return this.maxAccountLength;
    }

    public void setMaxAccountLength(final String maxAccountLength) {
        this.maxAccountLength = maxAccountLength;
    }

    public String getEmvKernelConfigurationSelection() {
        return this.emvKernelConfigurationSelection;
    }

    public void setEmvKernelConfigurationSelection(final String emvKernelConfigurationSelection) {
        this.emvKernelConfigurationSelection = emvKernelConfigurationSelection;
    }

    public String getTransactionDate() {
        return this.transactionDate;
    }

    public void setTransactionDate(final String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionTime() {
        return this.transactionTime;
    }

    public void setTransactionTime(final String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public void setCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCurrencyExponent() {
        return this.currencyExponent;
    }

    public void setCurrencyExponent(final String currencyExponent) {
        this.currencyExponent = currencyExponent;
    }

    public String getMerchantCategoryCode() {
        return this.merchantCategoryCode;
    }

    public void setMerchantCategoryCode(final String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public String getTransactionSequenceNumber() {
        return this.transactionSequenceNumber;
    }

    public void setTransactionSequenceNumber(final String transactionSequenceNumber) {
        this.transactionSequenceNumber = transactionSequenceNumber;
    }

    public String getTagList() {
        return this.tagList;
    }

    public void setTagList(final String tagList) {
        this.tagList = tagList;
    }

    public String getEdcType() {
        return edcType;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getVarValue() {
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public String getVarName1() {
        return varName1;
    }

    public void setVarName1(String varName1) {
        this.varName1 = varName1;
    }

    public String getVarValue1() {
        return varValue1;
    }

    public void setVarValue1(String varValue1) {
        this.varValue1 = varValue1;
    }

    public String getVarName2() {
        return varName2;
    }

    public void setVarName2(String varName2) {
        this.varName2 = varName2;
    }

    public String getVarValue2() {
        return varValue2;
    }

    public void setVarValue2(String varValue2) {
        this.varValue2 = varValue2;
    }

    public String getVarName3() {
        return varName3;
    }

    public void setVarName3(String varName3) {
        this.varName3 = varName3;
    }

    public String getVarValue3() {
        return varValue3;
    }

    public void setVarValue3(String varValue3) {
        this.varValue3 = varValue3;
    }

    public String getVarName4() {
        return varName4;
    }

    public void setVarName4(String varName4) {
        this.varName4 = varName4;
    }

    public String getVarValue4() {
        return varValue4;
    }

    public void setVarValue4(String varValue4) {
        this.varValue4 = varValue4;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getButton1() {
        return button1;
    }

    public void setButton1(String button1) {
        this.button1 = button1;
    }

    public String getButton2() {
        return button2;
    }

    public void setButton2(String button2) {
        this.button2 = button2;
    }

    public String getButton3() {
        return button3;
    }

    public void setButton3(String button3) {
        this.button3 = button3;
    }

    public String getButton4() {
        return button4;
    }

    public void setButton4(String button4) {
        this.button4 = button4;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public String getDisplayMessage2() {
        return displayMessage2;
    }

    public void setDisplayMessage2(String displayMessage2) {
        this.displayMessage2 = displayMessage2;
    }

    public String getTopDown() {
        return topDown;
    }

    public void setTopDown(String topDown) {
        this.topDown = topDown;
    }

    public String getTaxLine() {
        return taxLine;
    }

    public void setTaxLine(String taxLine) {
        this.taxLine = taxLine;
    }

    public String getTotalLine() {
        return totalLine;
    }

    public void setTotalLine(String totalLine) {
        this.totalLine = totalLine;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

    public String getItemData() {
        return itemData;
    }

    public void setItemData(String itemData) {
        this.itemData = itemData;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getHrefNum() {
        return hrefNum;
    }

    public void setHrefNum(String hrefNum) {
        this.hrefNum = hrefNum;
    }

    public String getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }

    public String getThankYouTitle() {
        return thankYouTitle;
    }

    public void setThankYouTitle(String thankYouTitle) {
        this.thankYouTitle = thankYouTitle;
    }

    public String getThankYouMessage1() {
        return thankYouMessage1;
    }

    public void setThankYouMessage1(String thankYouMessage1) {
        this.thankYouMessage1 = thankYouMessage1;
    }

    public String getThankYouMessage2() {
        return thankYouMessage2;
    }

    public void setThankYouMessage2(String thankYouMessage2) {
        this.thankYouMessage2 = thankYouMessage2;
    }

    public String getThankYouTimeOut() {
        return thankYouTimeOut;
    }

    public void setThankYouTimeOut(String thankYouTimeOut) {
        this.thankYouTimeOut = thankYouTimeOut;
    }

    public String getSigSavePath() {
        return sigSavePath;
    }

    public void setSigSavePath(String sigSavePath) {
        this.sigSavePath = sigSavePath;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public String getTargetDevice() {
        return this.targetDevice;
    }

    public void setTargetDevice(final String targetDevice) {
        this.targetDevice = targetDevice;
    }
}

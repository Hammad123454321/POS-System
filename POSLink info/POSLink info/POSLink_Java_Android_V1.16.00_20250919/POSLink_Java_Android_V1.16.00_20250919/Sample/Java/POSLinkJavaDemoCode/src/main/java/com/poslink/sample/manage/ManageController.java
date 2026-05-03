package com.poslink.sample.manage;

import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.POSLinkCommon;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.poslink.sample.common.model.BaseResponse;
import com.poslink.sample.main.MainController;
import com.poslink.sample.manage.model.ManageRequestEntity;
import com.poslink.sample.manage.model.ManageResponseEntity;
import com.poslink.sample.setting.model.CommSettingDao;
import com.poslink.sample.common.StringUtil;

/**
 * Created by Leon.F on 2018/3/9.
 */

public class ManageController {

    public ManageResponseEntity startManage(ManageRequestEntity requestEntity) {
        ManageResponseEntity response = new ManageResponseEntity();
        if (requestEntity != null) {
//            PosLink posLink = new PosLink();
            PosLink posLink = MainController.getInstance().createPosLink();
            posLink.SetCommSetting(CommSettingDao.read());
            ManageRequest manageRequest = new ManageRequest();
            manageRequest.TransType = manageRequest.ParseTransType(requestEntity.getTransType());
            manageRequest.EDCType = manageRequest.ParseEDCType(requestEntity.getEdcType());
            manageRequest.Trans = manageRequest.ParseTrans(requestEntity.getTrans());
            manageRequest.VarName = requestEntity.getVarName();
            manageRequest.VarName1 = requestEntity.getVarName1();
            manageRequest.VarName2 = requestEntity.getVarName2();
            manageRequest.VarName3 = requestEntity.getVarName3();
            manageRequest.VarName4 = requestEntity.getVarName4();
            manageRequest.VarValue = requestEntity.getVarValue();
            manageRequest.VarValue1 = requestEntity.getVarValue1();
            manageRequest.VarValue2 = requestEntity.getVarValue2();
            manageRequest.VarValue3 = requestEntity.getVarValue3();
            manageRequest.VarValue4 = requestEntity.getVarValue4();
            manageRequest.Title = requestEntity.getTitle();
            manageRequest.Button1 = requestEntity.getButton1();
            manageRequest.Button2 = requestEntity.getButton2();
            manageRequest.Button3 = requestEntity.getButton3();
            manageRequest.Button4 = requestEntity.getButton4();
            manageRequest.DisplayMessage = requestEntity.getDisplayMessage();
            manageRequest.DisplayMessage2 = requestEntity.getDisplayMessage2();
            manageRequest.TopDown = requestEntity.getTopDown();
            manageRequest.TaxLine = requestEntity.getTaxLine();
            manageRequest.TotalLine = requestEntity.getTotalLine();
            manageRequest.ImageDescription = requestEntity.getImageDescription();
            manageRequest.ItemData = requestEntity.getItemData();
            manageRequest.ImageName = requestEntity.getImageName();
            manageRequest.ImagePath = requestEntity.getImagePath();
            manageRequest.Upload = StringUtil.parseInt(requestEntity.getUpload());
            manageRequest.HRefNum = requestEntity.getHrefNum();
            manageRequest.TimeOut = requestEntity.getTimeOut();
            manageRequest.ThankYouTitle = requestEntity.getThankYouTitle();
            manageRequest.ThankYouMessage1 = requestEntity.getThankYouMessage1();
            manageRequest.ThankYouMessage2 = requestEntity.getThankYouMessage2();
            manageRequest.ThankYouTimeOut = requestEntity.getThankYouTimeOut();
            manageRequest.SigSavePath = requestEntity.getSigSavePath();

            manageRequest.Amount = requestEntity.getAmount();
            manageRequest.CashBackAmt = requestEntity.getCashBackAmt();
            manageRequest.MagneticSwipeEntryFlag = requestEntity.getMagneticSwipeEntryFlag();
            manageRequest.ManualEntryFlag = requestEntity.getManualEntryFlag();
            manageRequest.ContactlessEntryFlag = requestEntity.getContactlessEntryFlag();
            manageRequest.ContactEMVEntryFlag = requestEntity.getContactEMVEntryFlag();
            manageRequest.FallbackSwipeEntryFlag = requestEntity.getFallbackSwipeEntryFlag();
            manageRequest.ExpiryDatePrompt = requestEntity.getExpiryDatePrompt();
            manageRequest.CVVPrompt = requestEntity.getcvvPrompt();
            manageRequest.ZipCodePrompt = requestEntity.getZipCodePrompt();
            manageRequest.EncryptionFlag = requestEntity.getEncryptionFlag();
            manageRequest.KeySlot = requestEntity.getKeySlot();
            manageRequest.PaddingChar = requestEntity.getPaddingChar();
            manageRequest.TrackDataSentinel = requestEntity.getTrackDataSentinel();
            manageRequest.MINAccountLength = requestEntity.getMinAccountLength();
            manageRequest.MAXAccountLength = requestEntity.getMaxAccountLength();
            manageRequest.EmvKernelConfigurationSelection = requestEntity.getEmvKernelConfigurationSelection();
            manageRequest.TransactionDate = requestEntity.getTransactionDate();
            manageRequest.TransactionTime = requestEntity.getTransactionTime();
            manageRequest.CurrencyCode = requestEntity.getCurrencyCode();
            manageRequest.CurrencyExponent = requestEntity.getCurrencyExponent();
            manageRequest.MerchantCategoryCode = requestEntity.getMerchantCategoryCode();
            manageRequest.TransactionSequenceNumber = requestEntity.getTransactionSequenceNumber();
            manageRequest.TagList = requestEntity.getTagList();
            manageRequest.MerchantDecision = requestEntity.getMerchantDecision();
            manageRequest.EncryptionType = requestEntity.getEncryptionType();
            manageRequest.PinMinLength = requestEntity.getPinMinLength();
            manageRequest.PinMaxLength = requestEntity.getPinMaxLength();
            manageRequest.PINBypass = requestEntity.getPinBypass();
            manageRequest.PinAlgorithm = requestEntity.getPinAlgorithm();
            manageRequest.OnlineAuthorizationResult = requestEntity.getOnlineAuthorizationResult();
            manageRequest.ResponseCode = requestEntity.getResponseCode();
            manageRequest.AuthorizationCode = requestEntity.getAuthorizationCode();
            manageRequest.IssuerAuthenticationData = requestEntity.getIssuerAuthenticationData();
            manageRequest.IssuerScript1 = requestEntity.getIssuerScript1();
            manageRequest.IssuerScript2 = requestEntity.getIssuerScript2();
            manageRequest.AccountNumber = requestEntity.getAccountNumber();
            manageRequest.NullPin = requestEntity.getNullPIN();
            manageRequest.FilePath = requestEntity.getFilePath();
            manageRequest.FileType = requestEntity.getFileType();
            manageRequest.TargetDevice = requestEntity.getTargetDevice();
            posLink.ManageRequest = manageRequest;
            ProcessTransResult processTransResult = posLink.ProcessTrans();
            if (processTransResult.Code == ProcessTransResult.ProcessTransResultCode.OK) {
                response.setCode(BaseResponse.CODE_OK);
                response.setMessage(BaseResponse.MESSAGE_OK);
                ManageResponseEntity.Data data = new ManageResponseEntity.Data();
                ManageResponse manageResponse = posLink.ManageResponse;
                data.setResultCode(manageResponse.ResultCode);
                data.setResultTxt(manageResponse.ResultTxt);
                data.setSn(manageResponse.SN);
                data.setAuthorizationResult(manageResponse.AuthorizationResult);
                data.setButtonNum(manageResponse.ButtonNum);
                data.setCharsPerLine(manageResponse.CharsPerLine);
                data.setEMVData(manageResponse.EMVData);
                data.setEntryMode(manageResponse.EntryMode);
                data.setExpiryDate(manageResponse.ExpiryDate);
                data.setExtData(manageResponse.ExtData);
                data.setKsn(manageResponse.KSN);
                data.setLinesPerScreen(manageResponse.LinesPerScreen);
                data.setMacAddress(manageResponse.MacAddress);
                data.setModelName(manageResponse.ModelName);
                data.setPan(manageResponse.PAN);
                data.setPinBlock(manageResponse.PinBlock);
                data.setPrimaryFirmVersion(manageResponse.PrimaryFirmVersion);
                data.setSigFileName(manageResponse.SigFileName);
                data.setSignatureFlag(manageResponse.SignatureFlag);
                data.setStatus(manageResponse.CardInsertStatus);
                data.setQRCode(manageResponse.QRCode);
                data.setVarValue(manageResponse.VarValue);
                data.setVarValue1(manageResponse.VarValue1);
                data.setVarValue2(manageResponse.VarValue2);
                data.setVarValue3(manageResponse.VarValue3);
                data.setVarValue4(manageResponse.VarValue4);
                data.setTagList(manageResponse.TagList);
                data.setTrack1Data(manageResponse.Track1Data);
                data.setTrack2Data(manageResponse.Track2Data);
                data.setTrack3Data(manageResponse.Track3Data);
                data.setText(manageResponse.Text);
                data.setMaskedPAN(manageResponse.MaskedPAN);
                data.setBarcodeType(manageResponse.BarcodeType);
                data.setBarcodeData(manageResponse.BarcodeData);
                data.setEncryptionTransmissionBlock(manageResponse.EncryptionTransmissionBlock);
                data.setContactlessTransactionPath(manageResponse.ContactlessTransactionPath);
                data.setOnlinePINFlag(manageResponse.OnlinePINFlag);
                data.setEncryptedSensitiveTLVData(manageResponse.EncryptedSensitiveTLVData);
                data.setCardholder(manageResponse.CardHolderName);
                data.setServiceCode(manageResponse.ServiceCode);
                data.setCvvCode(manageResponse.CVVCode);
                data.setZipCode(manageResponse.ZipCode);
                data.setVasCode(manageResponse.VASResponseInfo.VASCode);
                data.setVasData(manageResponse.VASResponseInfo.VASData);
                data.setNDEFData(manageResponse.VASResponseInfo.NDEFData);
                data.setLuhnValidationResult(manageResponse.LuhnValidationResult);
                response.setData(data);
                return response;
            } else {
                response.setCode(BaseResponse.CODE_ERROR);
                response.setMessage(processTransResult.Msg);
                return response;
            }
        }

        response.setCode(BaseResponse.CODE_ERROR);
        response.setMessage(BaseResponse.MESSAGE_REQUEST_ERROR);
        return response;
    }
}

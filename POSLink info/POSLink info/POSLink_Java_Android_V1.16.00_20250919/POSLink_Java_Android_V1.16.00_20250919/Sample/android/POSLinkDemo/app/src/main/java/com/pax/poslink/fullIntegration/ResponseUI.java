package com.pax.poslink.fullIntegration;

import androidx.annotation.NonNull;

import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.fullIntegration.mock.MockResponse;
import com.pax.poslink.ui.pay.PaymentFragment;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.view.TitleItemEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Leon.F on 2018/5/15.
 */
public abstract class ResponseUI<T extends BaseResponse> {

    public abstract List<RenderEntity> createRenderList(T response);

    public static class InputAccountUI extends ResponseUI<InputAccount.InputAccountResponse> {

        @Override
        public List<RenderEntity> createRenderList(InputAccount.InputAccountResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Entry Mode", response.getEntryMode() == null ? "" :  String.valueOf(response.getEntryMode())),
                    new NameValueStringUnEditableEntity("Track1 Data", response.getTrack1Data()),
                    new NameValueStringUnEditableEntity("Track2 Data", response.getTrack2Data()),
                    new NameValueStringUnEditableEntity("Track3 Data", response.getTrack3Data()),
                    new NameValueStringUnEditableEntity("PAN", response.getPan()),
                    new NameValueStringUnEditableEntity("MaskedPAN", response.getMaskedPAN()),
                    new NameValueStringUnEditableEntity("KSN", response.getKsn()),
                    new NameValueStringUnEditableEntity("ETB", response.getEncryptionTransmissionBlock()),
                    new NameValueStringUnEditableEntity("ContactlessTransactionPath", response.getContactlessTransactionPath()),
                    new NameValueStringUnEditableEntity("Contactless Authorize Result", response.getAuthorizationResult()),
                    new NameValueStringUnEditableEntity("SignatureFlag", response.getSignatureFlag()),
                    new NameValueStringUnEditableEntity("OnlinePINFlag", response.getOnlinePINFlag()),
                    new NameValueStringUnEditableEntity("EMVData", response.getEmvData()),
                    new NameValueStringUnEditableEntity("EncryptedEMVTLVData", response.getEncryptedEMVTLVData()),
                    new NameValueStringUnEditableEntity("EncryptedSensitiveTLVData", response.getEncryptedSensitiveTLVData()),
                    new NameValueStringUnEditableEntity("Expiry Date", response.getExpiry()),
                    new NameValueStringUnEditableEntity("CardHolder", response.getCardholder()),
                    new NameValueStringUnEditableEntity("ServiceCode", response.getServiceCode()),
                    new NameValueStringUnEditableEntity("CVV", response.getCVVCode()),
                    new NameValueStringUnEditableEntity("Zip", response.getZipCode()),

                    new NameValueStringUnEditableEntity("VASCode", response.getVASCode()),
                    new NameValueStringUnEditableEntity("VASData", PaymentFragment.formatVASData(response.getVASData())),
                    new NameValueStringUnEditableEntity("NDEFData", response.getNDEFData()),
                    new NameValueStringUnEditableEntity("CVM", response.getCvm()),
                    new NameValueStringUnEditableEntity("Pinpad Type", response.getPinpadType()),
                    new NameValueStringUnEditableEntity("Luhn Validation Result", response.getLuhnValidationResult()),
                    new NameValueStringUnEditableEntity("CustomEncryptedData", getCustomEncryptedData(response)),
                    new NameValueStringUnEditableEntity("Custom MAC Data", response.getCustomMACInfo().getData()),
                    new NameValueStringUnEditableEntity("Custom MAC KSN", response.getCustomMACInfo().getKSN())


                    );
        }

        @NonNull
        private String getCustomEncryptedData(InputAccount.InputAccountResponse response) {
            return response.getCustomEncryptedData().isEmpty() ? "" : new ArrayList<>(response.getCustomEncryptedData()).toString();
        }
    }

    public static class SetEMVTLVUI extends ResponseUI<EMVTLVData.SetTLVResponse> {

        @Override
        public List<RenderEntity> createRenderList(EMVTLVData.SetTLVResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Tag List", response.getTagList())
            );
        }
    }


    public static class GetEMVTLVUI extends ResponseUI<EMVTLVData.GetTLVResponse> {

        @Override
        public List<RenderEntity> createRenderList(EMVTLVData.GetTLVResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("EMVData", response.getEmvData())
            );
        }
    }

    public static class RemoveCardUI extends ResponseUI<RemoveCard.RemoveCardResponse> {
        @Override
        public List<RenderEntity> createRenderList(RemoveCard.RemoveCardResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Pinpad Type", response.getPinpadType())
            );
        }
    }

    public static class CompleteOnlineEMVUI extends ResponseUI<CompleteOnlineEMV.CompleteOnlineEMVResponse> {
        @Override
        public List<RenderEntity> createRenderList(CompleteOnlineEMV.CompleteOnlineEMVResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Authorization Result", response.getAuthorizationResult()),
                    new NameValueStringUnEditableEntity("EMV TLV Data", response.getEmvData()),
                    new NameValueStringUnEditableEntity("Issuer Script Results", response.getIssuerScriptResults())
            );
        }
    }

    public static class AuthorizeCardUI extends ResponseUI<AuthorizeCard.AuthorizeResponse> {
        @Override
        public List<RenderEntity> createRenderList(AuthorizeCard.AuthorizeResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Authorization Result", response.getAuthorizationResult()),
                    new NameValueStringUnEditableEntity("Signature Flag", response.getSignatureFlag()),
                    new NameValueStringUnEditableEntity("PIN Block", response.getPinBlock()),
                    new NameValueStringUnEditableEntity("PIN Bypass Status", response.getPinBypassStatus()),
                    new NameValueStringUnEditableEntity("KSN", response.getKSN()),
                    new NameValueStringUnEditableEntity("EMV TLV Data", response.getEmvData()),
                    new NameValueStringUnEditableEntity("CVM", response.getCvm()),
                    new NameValueStringUnEditableEntity("Pinpad Type", response.getPinpadType())
            );
        }
    }

    public static class MockTransUI extends ResponseUI<MockResponse> {
        @Override
        public List<RenderEntity> createRenderList(MockResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Response", response.getResp())
            );
        }
    }

    public static class GetPINBlockUI extends ResponseUI<GetPINBlock.GetPINBlockResponse> {
        @Override
        public List<RenderEntity> createRenderList(GetPINBlock.GetPINBlockResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("PIN Block", response.getPinBlock()),
                    new NameValueStringUnEditableEntity("KSN", response.getKSN()),
                    new NameValueStringUnEditableEntity("Pinpad Type", response.getPinpadType())
            );
        }
    }

    public static class UpdateResourceUI extends ResponseUI<UpdateResource.UpdateResourceResponse> {
        @Override
        public List<RenderEntity> createRenderList(UpdateResource.UpdateResourceResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
            );
        }
    }

    public static class CheckFileUI extends ResponseUI<CheckFile.CheckFileResponse> {
        @Override
        public List<RenderEntity> createRenderList(CheckFile.CheckFileResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Check Sum", response.getChecksum())
            );
        }
    }

    public static class InitUI extends ResponseUI<Init.InitResponse> {
        @Override
        public List<RenderEntity> createRenderList(Init.InitResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("SN", response.getSn()),
                    new NameValueStringUnEditableEntity("ModelName", response.getModelName()),
                    new NameValueStringUnEditableEntity("OSVersion", response.getOsVersion()),
                    new NameValueStringUnEditableEntity("MacAddress", response.getMacAddress()),
                    new NameValueStringUnEditableEntity("LinesPerScreen", response.getLinesPerScreen()),
                    new NameValueStringUnEditableEntity("CharsPerLine", response.getCharsPerLine()),
                    new NameValueStringUnEditableEntity("AppName", response.getAppName()),
                    new NameValueStringUnEditableEntity("AppVersion", response.getAppVersion()),
                    new NameValueStringUnEditableEntity("WifiMac", response.getWifiMac()),
                    new NameValueStringUnEditableEntity("TouchScreen", response.getTouchscreen()),
                    new NameValueStringUnEditableEntity("Hardware Configuration Bitmap", response.getHardwareConfigurationBitmap()),
                    new NameValueStringUnEditableEntity("App Actived", response.getAppActivated()),
                    new NameValueStringUnEditableEntity("License Expiry", response.getLicenseExpiry())

            );
        }
    }

    public static class SetVarUI extends ResponseUI<Variable.SetVariableResponse> {
        @Override
        public List<RenderEntity> createRenderList(Variable.SetVariableResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
            );
        }
    }

    public static class GetVarUI extends ResponseUI<Variable.GetVariableResponse> {
        @Override
        public List<RenderEntity> createRenderList(Variable.GetVariableResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt()),
                    new NameValueStringUnEditableEntity("Var Value", response.getVariableValue()),
                    new NameValueStringUnEditableEntity("Var Value1", response.getVariableValue1()),
                    new NameValueStringUnEditableEntity("Var Value2", response.getVariableValue2()),
                    new NameValueStringUnEditableEntity("Var Value3", response.getVariableValue3()),
                    new NameValueStringUnEditableEntity("Var Value4", response.getVariableValue4())
                    );
        }
    }

    public static class VasSetParametersUI extends ResponseUI<VasSetMerchantParameters.VasSetMerchantParametersResponse> {
        @Override
        public List<RenderEntity> createRenderList(VasSetMerchantParameters.VasSetMerchantParametersResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
            );
        }
    }

    public static class VasPushDataUI extends ResponseUI<VasPushData.VasPushDataResponse> {
        @Override
        public List<RenderEntity> createRenderList(VasPushData.VasPushDataResponse response) {
            return Arrays.<RenderEntity>asList(
                    new NameValueStringUnEditableEntity("Result Code", response.getResultCode()),
                    new NameValueStringUnEditableEntity("Result Text", response.getResultTxt())
            );
        }
    }
}

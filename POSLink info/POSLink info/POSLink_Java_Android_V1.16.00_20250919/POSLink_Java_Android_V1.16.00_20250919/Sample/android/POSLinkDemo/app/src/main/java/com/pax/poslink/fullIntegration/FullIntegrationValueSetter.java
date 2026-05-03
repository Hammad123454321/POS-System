package com.pax.poslink.fullIntegration;

import com.google.gson.Gson;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.fullIntegration.mock.MockRequest;
import com.pax.poslink.model.manage.ManageItemValueSetter;
import com.pax.poslink.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Leon.F on 2018/5/15.
 */
public interface FullIntegrationValueSetter<T> {

    String SET_TLV_TYPE = "SET TLV Type";
    String CARD_TYPE_BITMAP = "CardTypeBitmap";
    String MOCK_HOST = "Host";
    String MOCK_PORT = "Port";
    String MOCK_PATH = "Path";
    String MOCK_EXPIRY = "expDate";
    String MOCK_TRACK2 = "track2";
    String MOCK_API_KEY = "apiKey";

    String RESOURCE_PATH = "PATH";
    String FILE_NAME = "File Name";
    String FILE_TYPE = "File Type";
    String TARGET_DEVICE = "Target Device";
    String TIMEOUT = "Timeout";
    String REPORT_STATUS = "Report Status";


    Map<String, FullIntegrationValueSetter> INPUT_ACCOUNT_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {

            put(ManageItemValueSetter.EMV_KERNEL_CONFIGURATION_SELECTION, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setEmvKernelConfigurationSelection(value);
                }
            });
            put(ManageItemValueSetter.TRANSACTION_DATE, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setTransactionDate(value);
                }
            });
            put(ManageItemValueSetter.TRANSACTION_TIME, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setTransactionTime(value);
                }
            });
            put(ManageItemValueSetter.CURRENCY_EXPONENT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setCurrencyExponent(value);
                }
            });
            put(ManageItemValueSetter.CURRENCY_CODE, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setCurrencyCode(value);
                }
            });
            put(ManageItemValueSetter.MAGNETIC_SWIPE_ENTRY_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setMagneticSwipeEntryFlag(value);
                }
            });
            put(ManageItemValueSetter.MANUAL_ENTRY_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setManualEntryFlag(value);
                }
            });
            put(ManageItemValueSetter.CONTACT_EMV_ENTRY_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setContactEMVEntryFlag(value);
                }
            });
            put(ManageItemValueSetter.CONTACTLESS_ENTRY_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setContactlessEntryFlag(value);
                }
            });
            put(ManageItemValueSetter.FALLBACK_SWIPE_ENTRY_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setFallbackSwipeEntryFlag(value);
                }
            });
            put(ManageItemValueSetter.CASH_BACK_AMOUNT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setCashBackAmt(value);
                }
            });
            put(ManageItemValueSetter.CVV_PROMPT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setCVVPrompt(value);
                }
            });
            put(ManageItemValueSetter.ZIP_PROMPT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setZipPrompt(value);
                }
            });
            put(ManageItemValueSetter.EXPIRY_DATE_PROMPT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setExpiryDatePrompt(value);
                }
            });
            put(ManageItemValueSetter.EDC_TYPE, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setEdcType(value);
                }
            });
            put(ManageItemValueSetter.TRANS_TYPE, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setTransType(value);
                }
            });
            put(ManageItemValueSetter.TIME_OUT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
//                    request.setTimeOut(StringUtil.parseInt(value));
                    request.setTimeOut(value);
                }
            });
            put(ManageItemValueSetter.AMOUNT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setAmount(value);
                }
            });

            put(ManageItemValueSetter.ENCRYPTION_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setEncryptionFlag(value);
                }
            });
            put(ManageItemValueSetter.KEY_SLOT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setKeySLot(value);
                }
            });

            put(ManageItemValueSetter.MIN_ACCOUNT_LENGTH, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setMinAccountLength(value);
                }
            });

            put(ManageItemValueSetter.MAX_ACCOUNT_LENGTH, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setMaxAccountLength(value);
                }
            });
            put(ManageItemValueSetter.TAG_LIST, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setTagList(value);
                }
            });
            put(ManageItemValueSetter.MERCHANT_CATEGORY_CODE, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setMerchantCategoryCode(value);
                }
            });
            put(ManageItemValueSetter.TRANSACTION_SEQUENCE_NUMBER, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setTransactionSequenceNumber(value);
                }
            });

            put(ManageItemValueSetter.PADDING_CHAR, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setPaddingChar(value);
                }
            });

            put(ManageItemValueSetter.TRACKDATA_SENTINEL, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setTrackDataSentinel(value);
                }
            });

            put(ManageItemValueSetter.CONTINUOUS_SCREEN, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setContinuousScreen(value);
                }
            });
            put(ManageItemValueSetter.FALLBACK_INSERT_ENTRY_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setFallbackInsertEntryFlag(value);
                }
            });
            put(ManageItemValueSetter.TRANSACTION_CVM_LIMIT, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.getTerminalConfiguration().setTransactionCVMLimit(value);
                }
            });
            put(ManageItemValueSetter.KSN_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setKsnFlag(value);
                }
            });
            put(FullIntegrationValueSetter.REPORT_STATUS, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setReportStatus(value);
                }
            });

            put(ManageItemValueSetter.CUSTOM_DATA, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    String[] data = value.split("\\|");
                    request.setCustomData(Arrays.asList(data));
                }
            });
            put(ManageItemValueSetter.FALLBACK_MANUAL_ENTRY_FLAG, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    request.setFallbackToManualEntryFlag(value);
                }
            });
            put(ManageItemValueSetter.Custom_MAC_Information, new FullIntegrationValueSetter<InputAccount.InputAccountRequest>() {
                @Override
                public void onSet(InputAccount.InputAccountRequest request, String value) {
                    Gson gson = new Gson();
                    ManageRequest.CustomMACInformation customMACInformation = gson.fromJson(value, ManageRequest.CustomMACInformation.class);
                    if (customMACInformation != null) {
                        InputAccount.CustomMACInformation macInformation = new InputAccount.CustomMACInformation();
                        macInformation.setKeySlot(customMACInformation.KeySlot);
                        macInformation.setKeyType(customMACInformation.KeyType);
                        macInformation.setData(customMACInformation.Data);
                        macInformation.setWorkMode(customMACInformation.WorkMode);
                        request.setCustomMACInformation(macInformation);
                    }
                }
            });
        }
    };

    Map<String, FullIntegrationValueSetter> REMOVE_CARD_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(ManageItemValueSetter.MESSAGE_1, new FullIntegrationValueSetter<RemoveCard.RemoveCardRequest>() {
                @Override
                public void onSet(RemoveCard.RemoveCardRequest request, String value) {
                    request.setMessage1(value);

                }
            });

            put(ManageItemValueSetter.MESSAGE_2, new FullIntegrationValueSetter<RemoveCard.RemoveCardRequest>() {
                @Override
                public void onSet(RemoveCard.RemoveCardRequest request, String value) {
                    request.setMessage2(value);
                }
            });

            put(ManageItemValueSetter.CONTINUOUS_SCREEN, new FullIntegrationValueSetter<RemoveCard.RemoveCardRequest>() {
                @Override
                public void onSet(RemoveCard.RemoveCardRequest request, String value) {
                    request.setContinuousScreen(value);
                }
            });

            put(ManageItemValueSetter.PINPAD_TYPE, new FullIntegrationValueSetter<RemoveCard.RemoveCardRequest>() {
                @Override
                public void onSet(RemoveCard.RemoveCardRequest request, String value) {
                    request.setPinpadType(value);
                }
            });
        }
    };

    Map<String, FullIntegrationValueSetter> SET_TLV_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(SET_TLV_TYPE, new FullIntegrationValueSetter<EMVTLVData.SetTLVRequest>() {
                @Override
                public void onSet(EMVTLVData.SetTLVRequest request, String value) {
                    request.setTLVType(value);
                }
            });

            put(ManageItemValueSetter.EMV_DATA, new FullIntegrationValueSetter<EMVTLVData.SetTLVRequest>() {
                @Override
                public void onSet(EMVTLVData.SetTLVRequest request, String value) {
                    request.setEMVData(value);
                }
            });

        }
    };

    Map<String, FullIntegrationValueSetter> GET_TLV_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {


            put(ManageItemValueSetter.TLV_TYPE, new FullIntegrationValueSetter<EMVTLVData.GetTLVRequest>() {
                @Override
                public void onSet(EMVTLVData.GetTLVRequest request, String value) {
                    request.setTlvType(value);
                }
            });


            put(ManageItemValueSetter.TAG_LIST, new FullIntegrationValueSetter<EMVTLVData.GetTLVRequest>() {
                @Override
                public void onSet(EMVTLVData.GetTLVRequest request, String value) {
                    request.setTagList(value);
                }
            });
        }
    };

    Map<String, FullIntegrationValueSetter> COMPLETE_EMV_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {

            put(ManageItemValueSetter.ONLINE_AUTH_RESULT, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setOnlineAuthorizationResult(value);
                }
            });
            put(ManageItemValueSetter.RESPONSE_CODE, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setResponseCode(value);
                }
            });
            put(ManageItemValueSetter.AUTHORIZATION_CODE, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setAuthorizationCode(value);
                }
            });
            put(ManageItemValueSetter.ISSUER_AUTH_DATA, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setIssuerAuthenticationData(value);
                }
            });
            put(ManageItemValueSetter.ISSUER_SCRIPT_1, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setIssuerScript1(value);
                }
            });
            put(ManageItemValueSetter.ISSUER_SCRIPT_2, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setIssuerScript2(value);
                }
            });
            put(ManageItemValueSetter.TAG_LIST, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setTagList(value);
                }
            });
            put(ManageItemValueSetter.CONTINUOUS_SCREEN, new FullIntegrationValueSetter<CompleteOnlineEMV.CompleteOnlineEMVRequest>() {
                @Override
                public void onSet(CompleteOnlineEMV.CompleteOnlineEMVRequest request, String value) {
                    request.setContinuousScreen(value);
                }
            });
        }
    };

    Map<String, FullIntegrationValueSetter> AUTHORIZE_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {

            put(ManageItemValueSetter.TAG_LIST, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setTagList(value);
                }
            });
            put(ManageItemValueSetter.EMV_KERNEL_CONFIGURATION_SELECTION, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.getTerminalConfiguration().setEmvKernelConfigurationSelection(value);
                }
            });
            put(ManageItemValueSetter.TRANSACTION_DATE, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.getTerminalConfiguration().setTransactionDate(value);
                }
            });
            put(ManageItemValueSetter.TRANSACTION_TIME, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.getTerminalConfiguration().setTransactionTime(value);
                }
            });
            put(ManageItemValueSetter.CURRENCY_EXPONENT, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.getTerminalConfiguration().setCurrencyExponent(value);
                }
            });
            put(ManageItemValueSetter.CURRENCY_CODE, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.getTerminalConfiguration().setCurrencyCode(value);
                }
            });
            put(ManageItemValueSetter.AMOUNT, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setAmount(value);
                }
            });

            put(ManageItemValueSetter.CASH_BACK_AMOUNT, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setCashBackAmt(value);
                }
            });
            put(ManageItemValueSetter.MERCHANT_DECISION, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setMerchantDecision(value);
                }
            });
            put(ManageItemValueSetter.KEY_SLOT, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setKeySlot(value);
                }
            });
            put(ManageItemValueSetter.PIN_MAX_LENGTH, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setPinMaxLength(value);
                }
            });
            put(ManageItemValueSetter.PIN_MIN_LENGTH, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setPinMinLength(value);
                }
            });
            put(ManageItemValueSetter.NULL_PIN_FLAG, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setPinBypassFlag(value);
                }
            });
            put(ManageItemValueSetter.PIN_ALGORITHM, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setPinAlgorithm(value);
                }
            });
            put(ManageItemValueSetter.TIME_OUT, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setTimeOut(StringUtil.parseInt(value));
                }
            });

            put(ManageItemValueSetter.ENCRYPTION_TYPE, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {

                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setPinEncryptionType(value);
                }
            });

            put(ManageItemValueSetter.MERCHANT_CATEGORY_CODE, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.getTerminalConfiguration().setMerchantCategoryCode(value);
                }
            });
            put(ManageItemValueSetter.TRANSACTION_SEQUENCE_NUMBER, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.getTerminalConfiguration().setTransactionSequenceNumber(value);
                }
            });
            put(ManageItemValueSetter.CONTINUOUS_SCREEN, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setContinuousScreen(value);
                }
            });
            put(ManageItemValueSetter.PINPAD_TYPE, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setPinpadType(value);
                }
            });
            put(ManageItemValueSetter.KSN_FLAG, new FullIntegrationValueSetter<AuthorizeCard.AuthorizeRequest>() {
                @Override
                public void onSet(AuthorizeCard.AuthorizeRequest request, String value) {
                    request.setKsnFlag(value);
                }
            });
        }
    };
    Map<String, FullIntegrationValueSetter> MOCK_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {

            put(MOCK_HOST, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setHost(value);
                }
            });

            put(MOCK_PORT, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setPort(StringUtil.parseInt(value));
                }
            });
            put(MOCK_PATH, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setPath(value);
                }
            });
            put(ManageItemValueSetter.AMOUNT, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setAmount(value);
                }
            });
            put(MOCK_EXPIRY, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setExpDate(value);
                }
            });
            put(MOCK_TRACK2, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setTrack2Data(value);
                }
            });
            put(MOCK_API_KEY, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setAPIKey(value);
                }
            });
            put(ManageItemValueSetter.EMV_DATA, new FullIntegrationValueSetter<MockRequest>() {
                @Override
                public void onSet(MockRequest request, String value) {
                    request.setEMVData(value);
                }
            });
        }
    };

    Map<String, FullIntegrationValueSetter> GET_PIN_BLOCK_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(ManageItemValueSetter.ACCOUNT_NUMBER, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {
                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setAccountNumber(value);
                }
            });
            put(ManageItemValueSetter.KEY_SLOT, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {

                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setKeySlot(value);
                }
            });
            put(ManageItemValueSetter.PIN_MAX_LENGTH, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {

                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setPinMaxLength(value);
                }
            });
            put(ManageItemValueSetter.PIN_MIN_LENGTH, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {

                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setPinMinLength(value);
                }
            });
            put(ManageItemValueSetter.NULL_PIN_FLAG, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {

                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setPinBypassFlag(value);
                }
            });
            put(ManageItemValueSetter.PIN_ALGORITHM, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {

                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setPinAlgorithm(value);
                }
            });
            put(ManageItemValueSetter.TIME_OUT, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {

                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setTimeOut100ms(StringUtil.parseInt(value));
                }
            });

            put(ManageItemValueSetter.ENCRYPTION_TYPE, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {

                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setEncryptionType(value);
                }
            });

            put(ManageItemValueSetter.EDC_TYPE, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {
                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setEdcType(value);
                }
            });
            put(ManageItemValueSetter.TRANS_TYPE, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {
                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setTransType(value);
                }
            });
            put(ManageItemValueSetter.TITLE, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {
                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setTitle(value);
                }
            });
            put(ManageItemValueSetter.PINPAD_TYPE, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {
                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setPinpadType(value);
                }
            });
            put(ManageItemValueSetter.KSN_FLAG, new FullIntegrationValueSetter<GetPINBlock.GetPINBlockRequest>() {
                @Override
                public void onSet(GetPINBlock.GetPINBlockRequest request, String value) {
                    request.setKsnFlag(value);
                }
            });
        }
    };
    Map<String,FullIntegrationValueSetter> UPDATE_RESOURCE_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(RESOURCE_PATH, new FullIntegrationValueSetter<UpdateResource.UpdateResourceRequest>() {
                @Override
                public void onSet(UpdateResource.UpdateResourceRequest request, String value) {
                    request.setFile(new File(value));
                }
            });
            put(FILE_TYPE,new FullIntegrationValueSetter<UpdateResource.UpdateResourceRequest>() {
                @Override
                public void onSet(UpdateResource.UpdateResourceRequest request, String value) {
                    request.setFileType(value);
                }
            });
            put(TARGET_DEVICE,new FullIntegrationValueSetter<UpdateResource.UpdateResourceRequest>() {
                @Override
                public void onSet(UpdateResource.UpdateResourceRequest request, String value) {
                    request.setTargetDevice(value);
                }
            });
            put(TIMEOUT,new FullIntegrationValueSetter<UpdateResource.UpdateResourceRequest>() {
                @Override
                public void onSet(UpdateResource.UpdateResourceRequest request, String value) {
                    request.setTimeout(value);
                }
            });
        }
    };

    Map<String,FullIntegrationValueSetter> CHECK_FILE_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(FILE_NAME, new FullIntegrationValueSetter<CheckFile.CheckFileRequest>() {
                @Override
                public void onSet(CheckFile.CheckFileRequest request, String value) {
                    request.setFileName(value);
                }
            });
        }
    };

    Map<String,FullIntegrationValueSetter> SET_VAR_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(ManageItemValueSetter.EDC_TYPE, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {

                }
            });
            put(ManageItemValueSetter.VAR_NAME, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableName(value);
                }
            });
            put(ManageItemValueSetter.VAR_VALUE, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableValue(value);
                }
            });
            put(ManageItemValueSetter.VAR_NAME1, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableName1(value);
                }
            });
            put(ManageItemValueSetter.VAR_VALUE1, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableValue1(value);
                }
            });
            put(ManageItemValueSetter.VAR_NAME2, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableName2(value);
                }
            });
            put(ManageItemValueSetter.VAR_VALUE2, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableValue2(value);
                }
            });
            put(ManageItemValueSetter.VAR_NAME3, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableName3(value);
                }
            });
            put(ManageItemValueSetter.VAR_VALUE3, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableValue3(value);
                }
            });
            put(ManageItemValueSetter.VAR_NAME4, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableName4(value);
                }
            });
            put(ManageItemValueSetter.VAR_VALUE4, new FullIntegrationValueSetter<Variable.SetVariableRequest>() {

                @Override
                public void onSet(Variable.SetVariableRequest request, String value) {
                    request.setVariableValue4(value);
                }
            });
        }
    };

    Map<String,FullIntegrationValueSetter> GET_VAR_MAP = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(ManageItemValueSetter.EDC_TYPE, new FullIntegrationValueSetter<Variable.GetVariableRequest>() {

                @Override
                public void onSet(Variable.GetVariableRequest request, String value) {
                    request.setEdcType(value);
                }
            });

            put(ManageItemValueSetter.VAR_NAME, new FullIntegrationValueSetter<Variable.GetVariableRequest>() {

                @Override
                public void onSet(Variable.GetVariableRequest request, String value) {
                    request.setVariableName(value);
                }
            });

            put(ManageItemValueSetter.VAR_NAME1, new FullIntegrationValueSetter<Variable.GetVariableRequest>() {

                @Override
                public void onSet(Variable.GetVariableRequest request, String value) {
                    request.setVariableName1(value);
                }
            });

            put(ManageItemValueSetter.VAR_NAME2, new FullIntegrationValueSetter<Variable.GetVariableRequest>() {

                @Override
                public void onSet(Variable.GetVariableRequest request, String value) {
                    request.setVariableName2(value);
                }
            });

            put(ManageItemValueSetter.VAR_NAME3, new FullIntegrationValueSetter<Variable.GetVariableRequest>() {

                @Override
                public void onSet(Variable.GetVariableRequest request, String value) {
                    request.setVariableName3(value);
                }
            });

            put(ManageItemValueSetter.VAR_NAME4, new FullIntegrationValueSetter<Variable.GetVariableRequest>() {

                @Override
                public void onSet(Variable.GetVariableRequest request, String value) {
                    request.setVariableName4(value);
                }
            });
        }
    };

    Map<String,FullIntegrationValueSetter> VAS_SET_PARAMETERS = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(ManageItemValueSetter.VAS_PROGRAM, new FullIntegrationValueSetter<VasSetMerchantParameters.VasSetMerchantParametersRequest>() {
                @Override
                public void onSet(VasSetMerchantParameters.VasSetMerchantParametersRequest request, String value) {
                    request.setVasProgram(value);
                }
            });
            put(ManageItemValueSetter.VAS_MODE, new FullIntegrationValueSetter<VasSetMerchantParameters.VasSetMerchantParametersRequest>() {
                @Override
                public void onSet(VasSetMerchantParameters.VasSetMerchantParametersRequest request, String value) {
                    request.setVasMode(value);
                }
            });
            put(ManageItemValueSetter.VAS_SPECIAL_DATA, new FullIntegrationValueSetter<VasSetMerchantParameters.VasSetMerchantParametersRequest>() {
                @Override
                public void onSet(VasSetMerchantParameters.VasSetMerchantParametersRequest request, String value) {
                    Gson gson = new Gson();
                    if ("1".equals(request.getVasProgram())) {
                        ManageRequest.ApplePayVAS applePayVAS = gson.fromJson(value, ManageRequest.ApplePayVAS.class);
                        if (applePayVAS != null) {
                            VasSetMerchantParameters.ApplePayVAS payVAS = new VasSetMerchantParameters.ApplePayVAS();
                            payVAS.setMerchantId(applePayVAS.MerchantID);
                            payVAS.setUrl(applePayVAS.Url);
                            payVAS.setUrlMode(applePayVAS.UrlMode);
                            request.setApplePayVas(payVAS);
                        }
                    } else if ("2".equals(request.getVasProgram())) {
                        ManageRequest.GoogleSmartTap googleSmartTap = gson.fromJson(value, ManageRequest.GoogleSmartTap.class);
                        if (googleSmartTap != null) {
                            VasSetMerchantParameters.GoogleSmartTap smartTap = new VasSetMerchantParameters.GoogleSmartTap();
                            smartTap.setCollectId(googleSmartTap.CollectID);
                            smartTap.setEndTap(googleSmartTap.EndTap);
                            smartTap.setGoogleSmartTapCap(googleSmartTap.GoogleSmartTapCap);
                            smartTap.setMerchantCategory(googleSmartTap.MerchantCategory);
                            smartTap.setMerchantName(googleSmartTap.MerchantName);
                            smartTap.setOseToPpse(googleSmartTap.OseToPpse);
                            smartTap.setSecurity(googleSmartTap.Security);
                            smartTap.setServiceType(googleSmartTap.ServiceType);
                            smartTap.setStoreLocalId(googleSmartTap.StoreLocalID);
                            smartTap.setTerminalId(googleSmartTap.TerminalID);
                            request.setGoogleSmartTap(smartTap);
                        }
                    }
                }
            });
        }
    };

    Map<String,FullIntegrationValueSetter> VAS_PUSH_DATA = new HashMap<String, FullIntegrationValueSetter>() {
        {
            put(ManageItemValueSetter.VAS_MODE, new FullIntegrationValueSetter<VasPushData.VasPushDataRequest>() {
                @Override
                public void onSet(VasPushData.VasPushDataRequest request, String value) {
                    request.setVasMode(value);
                }
            });

            put(ManageItemValueSetter.VAS_SMART_TAP_DATA, new FullIntegrationValueSetter<VasPushData.VasPushDataRequest>() {
                @Override
                public void onSet(VasPushData.VasPushDataRequest request, String value) {
                    Gson gson = new Gson();
                    ManageRequest.GoogleSmartTapPushService googleSmartTapPushService = gson.fromJson(value, ManageRequest.GoogleSmartTapPushService.class);
                    VasPushData.GoogleSmartTapPushService smartTapPushService = new VasPushData.GoogleSmartTapPushService();
                    if (googleSmartTapPushService != null) {
                        smartTapPushService.setCollectId(googleSmartTapPushService.CollectID);
                        smartTapPushService.setEndTap(googleSmartTapPushService.EndTap);
                        smartTapPushService.setGoogleSmartTapCap(googleSmartTapPushService.GoogleSmartTapCap);
                        smartTapPushService.setSecurity(googleSmartTapPushService.Security);
                        List<VasPushData.GoogleSmartTapPushService.ServiceUsage> serviceUsageList = new ArrayList<>();
                        for (ManageRequest.GoogleSmartTapPushService.ServiceUsage serviceUsage : googleSmartTapPushService.ServiceUsages) {
                            VasPushData.GoogleSmartTapPushService.ServiceUsage usage = new VasPushData.GoogleSmartTapPushService.ServiceUsage();
                            usage.setDescribe(serviceUsage.Describe);
                            usage.setState(serviceUsage.State);
                            usage.setTitle(serviceUsage.Title);
                            usage.setUsageId(serviceUsage.UsageID);
                            serviceUsageList.add(usage);
                        }
                        smartTapPushService.setServiceUsages(serviceUsageList);

                        List<VasPushData.GoogleSmartTapPushService.ServiceUpdate> serviceUpdateList = new ArrayList<>();
                        for (ManageRequest.GoogleSmartTapPushService.ServiceUpdate serviceUpdate : googleSmartTapPushService.ServiceUpdates) {
                            VasPushData.GoogleSmartTapPushService.ServiceUpdate update = new VasPushData.GoogleSmartTapPushService.ServiceUpdate();
                            update.setUpdateId(serviceUpdate.UpdateID);
                            update.setUpdateOperation(serviceUpdate.UpdateOperation);
                            update.setUpdatePayload(serviceUpdate.UpdatePayload);
                            serviceUpdateList.add(update);
                        }
                        smartTapPushService.setServiceUpdates(serviceUpdateList);

                        List<VasPushData.GoogleSmartTapPushService.NewService> newServiceList = new ArrayList<>();
                        for (ManageRequest.GoogleSmartTapPushService.NewService newService : googleSmartTapPushService.NewServices) {
                            VasPushData.GoogleSmartTapPushService.NewService service = new VasPushData.GoogleSmartTapPushService.NewService();
                            service.setTitle(newService.Title);
                            service.setType(newService.Type);
                            service.setUri(newService.Uri);
                            newServiceList.add(service);
                        }
                        smartTapPushService.setNewServices(newServiceList);
                        request.setGoogleSmartTapPushService(smartTapPushService);
                    }
                }
            });
        }
    };


    void onSet(T request, String value);
}

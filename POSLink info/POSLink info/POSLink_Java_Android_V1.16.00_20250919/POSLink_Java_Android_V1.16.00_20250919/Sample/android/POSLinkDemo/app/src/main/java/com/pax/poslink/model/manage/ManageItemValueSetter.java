package com.pax.poslink.model.manage;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.entity.MultiMerchant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon.F on 2018/1/15.
 */
public interface ManageItemValueSetter<T> {
    String VAR_NAME = "Var Name";
    String VAR_VALUE = "Var Value";
    String VAR_NAME1 = "Var Name1";
    String VAR_VALUE1 = "Var Value1";
    String VAR_NAME2 = "Var Name2";
    String VAR_VALUE2 = "Var Value2";
    String VAR_NAME3 = "Var Name3";
    String VAR_NAME4 = "Var Name4";
    String VAR_VALUE3 = "Var Value3";
    String VAR_VALUE4 = "Var Value4";
    String EDC_TYPE = "EDC Type";
    String EXT_DATA = "Ext Data";
    String TITLE = "Title";
    String THANK_YOU_TITLE = "ThankYou Title";
    String BUTTON_1 = "Button1";
    String BUTTON_2 = "Button2";
    String BUTTON_3 = "Button3";
    String BUTTON_4 = "Button4";
    String SAVE_SIG_PATH = "Save sig path";
    String TIME_OUT = "TimeOut";
    String THANK_YOU_TIME_OUT = "ThankYou TimeOut";
    String DISPLAY_MESSAGE = "Display Message";
    String DISPLAY_MESSAGE2 = "Display Message2";
    String FILE_PATH = "File Path";
    String UPLOAD = "Upload";
    String H_REF_NUM = "HRefNum";
    String IMAGE_NAME = "Image Name";
    String IMAGE_DESCRIPTION = "Image Description";
    String THANK_YOU_MESSAGE1 = "ThankYou Message1";
    String THANK_YOU_MESSAGE2 = "ThankYou Message2";
    String ACCOUNT_NUMBER = "Account Number";
    String ENCRYPTION_TYPE = "Encryption Type";
    String KEY_SLOT = "Key Slot";
    String PIN_MIN_LENGTH = "PIN Min Length";
    String PIN_MAX_LENGTH = "PIN Max Length";
    String NULL_PIN_FLAG = "Null PIN";
    String PIN_BYPASS = "PIN Bypass";
    String PIN_ALGORITHM = "PIN Algorithm";
    String MAGNETIC_SWIPE_ENTRY_FLAG = "Magnetic Swipe Entry Flag";
    String MANUAL_ENTRY_FLAG = "Manual Entry Flag";
    String CONTACTLESS_ENTRY_FLAG = "Contactless Entry Flag";
    String SCANNER_ENTRY_FLAG = "Scanner Entry Flag";
    String EXPIRY_DATE_PROMPT = "Expiry Date Prompt";
    String ENCRYPTION_FLAG = "Encryption Flag";
    String PADDING_CHAR = "Padding Char";
    String MAC_KEY_TYPE = "MAC Key Type";
    String KSN_FLAG = "KSN Flag";
    String TRACKDATA_SENTINEL = "TrackData Sentinel";
    String CONTACT_EMV_ENTRY_FLAG = "Contact EMV Entry Flag";
    String FALLBACK_SWIPE_ENTRY_FLAG = "Fallback Swipe Entry Flag";
    String FALLBACK_INSERT_ENTRY_FLAG = "Fallback Insert Entry Flag";
    String TRANSACTION_CVM_LIMIT = "Transaction CVM Limit";
    String MIN_ACCOUNT_LENGTH = "Min Account Length";
    String MAX_ACCOUNT_LENGTH = "Max Account Length";
    String INPUT_TYPE = "Input Type";
    String MIN_LENGTH = "Min Length";
    String MAX_LENGTH = "Max length";
    String DEFAULT_VALUE = "Default Value";
    String FILE_NAME = "File Name";
    String AMOUNT = "Amount";
    String TIP_AMOUNT = "Tip Amount";
    String CASH_BACK_AMOUNT = "CashBack Amount";
    String SURCHARGE_AMT = "SurchargeAmt";
    String TAX_AMOUNT = "Tax Amount";
    String MERCHANT_DECISION = "Merchant Decision";
    String CURRENCY_CODE = "Currency Code";
    String COUNTRY_CODE = "Country Code";
    String ONLINE_AUTH_RESULT = "Online Auth Result";
    String RESPONSE_CODE = "Response Code";
    String AUTHORIZATION_CODE = "Authorization Code";
    String ISSUER_AUTH_DATA = "Issuer Auth Data";
    String ISSUER_SCRIPT_1 = "Issuer Script 1";
    String ISSUER_SCRIPT_2 = "Issuer Script 2";
    String MESSAGE_1 = "Message1";
    String MESSAGE_2 = "Message2";
    String TAG_LIST = "Tag List";
    String TLV_TYPE = "TLV Type";
    String EMV_DATA = "EMV Data";
    String SAF_MODE = "SAF Mode";
    String TEXT = "Text";
    String BUTTON_COLOR1 = "Button Color1";
    String BUTTON_COLOR2 = "Button Color2";
    String BUTTON_COLOR3 = "Button Color3";
    String START_DATE_TIME = "Start Date Time";
    String END_DATE_TIME = "End Date Time";
    String DURATION_IN_DAYS = "Duration In Days";
    String MAX_NUMBER = "Max Number";
    String TOTAL_CEILING_AMOUNT = "Total Ceiling Amount";
    String CEILING_AMOUNT_PER_CARD_TYPE = "Ceiling Amount Per Card Type";
    String HALO_PER_CARD_TYPE = "HALO Per Card Type";
    String SAF_UPLOAD_MODE = "SAF Upload Mode";
    String AUTO_UPLOAD_INTERVAL_TIME = "Auto Upload Interval time";
    String DELETE_SAF_CONFIRMATION = "Delete SAF Confirmation";
    String BUTTON_KEY1 = "Button Key1";
    String BUTTON_KEY2 = "Button Key2";
    String BUTTON_KEY3 = "Button Key3";
    String ENABLE_HARD_KEY = "Enable HardKey";
    String HARD_KEY_LIST = "HardKeyList";
    String LAST_RECEIPT = "LastReceipt";
    String REF_NUM = "RefNum";
    String ECR_REF_NUM = "ECRRefNum";
    String PRINT_COPY = "Print Copy";
    String PRINT_DATA = "Print Data";
    String TOPDOWN = "Topdown";
    String TAX_LINE = "Tax Line";
    String TOTAL_LINE = "Total Line";
    String LINE_ITEM_ACTION = "Line Item Action";
    String ITEM_INDEX = "Item Index";
    String ITEMS_PASSTHRUDATA = "Items Passthrudata";
    String TRANS_TYPE = "Trans Type";

    String CVV_PROMPT = "CVV Prompt";
    String ZIP_PROMPT = "Zip Prompt";
    String EMV_KERNEL_CONFIGURATION_SELECTION = "EMV Kernel Config Selection";
    String TRANSACTION_DATE = "Transaction Date";
    String TRANSACTION_TIME = "Transaction Time";
    String CURRENCY_EXPONENT = "Currency Exponent";
    String MERCHANT_CATEGORY_CODE = "Merchant Category Code";
    String TRANSACTION_SEQUENCE_NUMBER = "Transaction Sequence Number";


    String LABEL1 = "Label1";
    String LABEL2 = "Label2";
    String LABEL3 = "Label3";
    String LABEL4 = "Label4";
    String LABEL1_PROPERTY = "Label1 Property";
    String LABEL2_PROPERTY = "Label2 Property";
    String LABEL3_PROPERTY = "Label3 Property";
    String LABEL4_PROPERTY = "Label4 Property";
    String BUTTON_TYPE = "Button Type";

    String SIGNATURE_BOX = "Signature Box";
    String CAMERA_SCAN_READER = "CameraScanReader";

    String VAS_PROGRAM = "VAS Program";
    String VAS_MODE = "VAS Mode";
    String VAS_SPECIAL_DATA = "VAS Special Data";
    String VAS_SMART_TAP_DATA = "VAS Data";

    String TOKEN_COMMAND = "TOKEN COMMAND";
    String TOKEN = "TOKEN";
    String TOKENSN = "TOKENSN";

    String EXPIRY_DATE = "Expiry Date";

    String M1COMMAND = "M1Command";
    String BLOCKNO = "Block No";
    String ACCNT_CIPHER = "Password";
    String ACCNT_CIPHER_TYPE = "Password Type";
    String BLOCK_VALUE = "Block Value";
    String UPDATEBLKNO = "Update Block No";

    String CONTINUOUS_SCREEN = "ContinuousScreen";
    String PINPAD_TYPE = "Pinpad Type";
    String ICON_NAME = "Icon Name";

    String SOURCE_KEY_TYPE = "Source Key Type";
    String SOURCE_KEY_INDEX = "Source Key Index";
    String DESTINATION_KEY_TYPE = "Destination Key Type";
    String DESTINATION_KEY_INDEX = "Destination key Index";
    String DESTINATION_KEY_VALUE = "Destination key Value";
    String CHECK_MODE = "Check Mode";
    String CHECK_BUFFER = "Check Buffer";

    String INPUT_DATA = "Input Data";
    String ENCRYPTION_BITMAP = "Encryption Bitmap";
    String MAC_KEY_SLOT = "MAC Key Slot";
    String MAC_WORK_MODE = "MAC work mode";
    String ENCRYPTION_KEY_SLOT = "Encryption Key Slot";

    String MULTI_MERCHANT = "Multi Merchant";
    String KEY_TYPE = "Key Type";

    String BARCODE_TYPE = "Barcode Type";
    String BARCODE_DATA = "Barcode Data";
    String FILE_TYPE = "File Type";
    String TARGET_DEVICE = "Target Device";
    String RECEIPT_PRINT = "Receipt Print";
    String CUSTOM_DATA = "Custom Data";
    String Custom_MAC_Information = "Custom MAC Information";
    String INPUT_TEXT_TITLE = "Input Text Title";
    String INPUT_TEXT = "Input Text";
    //    String ENABLE_LUHN_CHECK = "Enable Luhn Check";
    String FALLBACK_MANUAL_ENTRY_FLAG = "Fallback To Manual Entry Flag";
    String DATA_ENCRYPTION_TYPE = "Data Encryption Type";
    String WORK_MODE = "Work Mode";
    String KEY_INDEX = "Key Index";
    String USER_DATA = "User Data";
    String INIT_VECTOR = "Init Vector";

    Map<String, ManageItemValueSetter> VALUE_SETTER_MAP = new HashMap<String, ManageItemValueSetter>() {
        {

            put(EDC_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EDCType = request.ParseEDCType(value);
                }
            });

            put(TRANS_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Trans = request.ParseTrans(value);
                }
            });

            put(EXT_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ExtData = value;
                }
            });

            put(VAR_NAME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarName = value;
                }
            });

            put(VAR_NAME1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarName1 = value;
                }
            });

            put(VAR_NAME2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarName2 = value;
                }
            });

            put(VAR_NAME3, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarName3 = value;
                }
            });

            put(VAR_NAME4, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarName4 = value;
                }
            });


            put(VAR_VALUE1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarValue1 = value;
                }
            });

            put(VAR_VALUE2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarValue2 = value;
                }
            });

            put(VAR_VALUE3, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarValue3 = value;
                }
            });

            put(VAR_VALUE4, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarValue4 = value;
                }
            });

            put(VAR_VALUE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VarValue = value;
                }
            });

            put(TITLE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Title = value;
                }
            });

            put(THANK_YOU_TITLE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ThankYouTitle = value;
                }
            });

            put(BUTTON_1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Button1 = value;
                }
            });

            put(BUTTON_2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Button2 = value;
                }
            });

            put(BUTTON_3, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Button3 = value;
                }
            });
            put(BUTTON_4, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Button4 = value;
                }
            });
            put(SAVE_SIG_PATH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.SigSavePath = value;
                }
            });

            put(TIME_OUT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TimeOut = value;
                }
            });

            put(THANK_YOU_TIME_OUT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ThankYouTimeOut = value;
                }
            });

            put(DISPLAY_MESSAGE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DisplayMessage = value;
                }
            });

            put(DISPLAY_MESSAGE2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DisplayMessage2 = value;
                }
            });

            put(FILE_PATH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.FilePath = value;
                }
            });
            put(FILE_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.FileType = value;
                }
            });
            put(TARGET_DEVICE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TargetDevice = value;
                }
            });
            put(UPLOAD, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    if (!TextUtils.isEmpty(value) && TextUtils.isDigitsOnly(value)) {
                        request.Upload = Integer.parseInt(value);
                    }
                }
            });

            put(H_REF_NUM, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.HRefNum = value;
                }
            });

            put(IMAGE_NAME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ImageName = value;
                }
            });

            put(IMAGE_DESCRIPTION, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ImageDescription = value;
                }
            });

            put(THANK_YOU_MESSAGE1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ThankYouMessage1 = value;
                }
            });

            put(THANK_YOU_MESSAGE2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ThankYouMessage2 = value;
                }
            });

            put(ACCOUNT_NUMBER, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.AccountNumber = value;
                }
            });
            put(ENCRYPTION_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EncryptionType = value;
                }
            });
            put(KEY_SLOT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.KeySlot = value;
                }
            });
            put(PIN_MIN_LENGTH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PinMinLength = value;
                }
            });
            put(MAGNETIC_SWIPE_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MagneticSwipeEntryFlag = value;
                }
            });
            put(MANUAL_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ManualEntryFlag = value;
                }
            });
            put(CONTACTLESS_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ContactlessEntryFlag = value;
                }
            });
            put(SCANNER_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ScannerEntryFlag = value;
                }
            });
            put(EXPIRY_DATE_PROMPT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ExpiryDatePrompt = value;
                }
            });
            put(ENCRYPTION_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EncryptionFlag = value;
                }
            });
            put(CONTACT_EMV_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ContactEMVEntryFlag = value;
                }
            });
            put(FALLBACK_SWIPE_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.FallbackSwipeEntryFlag = value;
                }
            });
            put(MIN_ACCOUNT_LENGTH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MINAccountLength = value;
                }
            });
            put(MAX_ACCOUNT_LENGTH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MAXAccountLength = value;
                }
            });
            put(INPUT_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.InputType = value;
                }
            });
            put(MIN_LENGTH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MINLength = value;
                }
            });
            put(MAX_LENGTH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MAXLength = value;
                }
            });
            put(DEFAULT_VALUE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DefaultValue = value;
                }
            });
            put(FILE_NAME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.FileName = value;
                }
            });
            put(AMOUNT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Amount = value;
                }
            });
            put(TIP_AMOUNT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TipAmt = value;
                }
            });
            put(CASH_BACK_AMOUNT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CashBackAmt = value;
                }
            });
            put(SURCHARGE_AMT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.SurchargeAmt = value;
                }
            });
            put(TAX_AMOUNT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TaxAmt = value;
                }
            });
            put(MERCHANT_DECISION, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MerchantDecision = value;
                }
            });
            put(PIN_MAX_LENGTH, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PinMaxLength = value;
                }
            });
            put(NULL_PIN_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.NullPin = value;
                }
            });
            put(PIN_BYPASS, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PINBypass = value;
                }
            });
            put(PIN_ALGORITHM, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PinAlgorithm = value;
                }
            });
            put(CURRENCY_CODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CurrencyCode = value;
                }
            });
            put(COUNTRY_CODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CountryCode = value;
                }
            });
            put(ONLINE_AUTH_RESULT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.OnlineAuthorizationResult = value;
                }
            });
            put(RESPONSE_CODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ResponseCode = value;
                }
            });
            put(AUTHORIZATION_CODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.AuthorizationCode = value;
                }
            });
            put(ISSUER_SCRIPT_1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.IssuerScript1 = value;
                }
            });
            put(ISSUER_SCRIPT_2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.IssuerScript2 = value;
                }
            });
            put(MESSAGE_1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Message1 = value;
                }
            });
            put(MESSAGE_2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Message2 = value;
                }
            });
            put(TAG_LIST, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TagList = value;
                }
            });
            put(TLV_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TLVType = value;
                }
            });
            put(EMV_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EMVData = value;
                }
            });
            put(ISSUER_AUTH_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.IssuerAuthenticationData = value;
                }
            });
            put(SAF_MODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.SAFMode = value;
                }
            });
            put(START_DATE_TIME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.StartDateTime = value;
                }
            });
            put(END_DATE_TIME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EndDateTime = value;
                }
            });
            put(DURATION_IN_DAYS, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DurationInDays = value;
                }
            });
            put(MAX_NUMBER, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MaxNumber = value;
                }
            });
            put(TOTAL_CEILING_AMOUNT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TotalCeilingAmount = value;
                }
            });
            put(CEILING_AMOUNT_PER_CARD_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CeilingAmountPerCardType = value;
                }
            });
            put(HALO_PER_CARD_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.HALOPerCardType = value;
                }
            });
            put(SAF_UPLOAD_MODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.SAFUploadMode = value;
                }
            });
            put(AUTO_UPLOAD_INTERVAL_TIME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.AutoUploadIntervalTime = value;
                }
            });
            put(DELETE_SAF_CONFIRMATION, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DeleteSAFConfirmation = value;
                }
            });
            put(TEXT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Text = value;
                }
            });
            put(BUTTON_COLOR1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ButtonColor1 = value;
                }
            });
            put(BUTTON_COLOR2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ButtonColor2 = value;
                }
            });
            put(BUTTON_COLOR3, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ButtonColor3 = value;
                }
            });
            put(BUTTON_KEY1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ButtonKey1 = value;
                }
            });
            put(BUTTON_KEY2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ButtonKey2 = value;
                }
            });
            put(BUTTON_KEY3, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ButtonKey3 = value;
                }
            });
            put(ENABLE_HARD_KEY, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EnableHardKey = value;
                }
            });
            put(HARD_KEY_LIST, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.HardKeyList = value;
                }
            });
            put(LAST_RECEIPT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.LastReceipt = value;
                }
            });
            put(REF_NUM, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.RefNum = value;
                }
            });
            put(ECR_REF_NUM, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ECRRefNum = value;
                }
            });
            put(PRINT_COPY, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PrintCopy = value;
                }
            });
            put(PRINT_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PrintData = value;
                }
            });
            put(TOPDOWN, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TopDown = value;
                }
            });
            put(TAX_LINE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TaxLine = value;
                }
            });
            put(TOTAL_LINE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TotalLine = value;
                }
            });
            put(ITEMS_PASSTHRUDATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ItemData = value;
                }
            });
            put(LINE_ITEM_ACTION, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.LineItemAction = value;
                }
            });
            put(ITEM_INDEX, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ItemIndex = value;
                }
            });
            put(EMV_KERNEL_CONFIGURATION_SELECTION, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EmvKernelConfigurationSelection = value;
                }
            });
            put(TRANSACTION_DATE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TransactionDate = value;
                }
            });
            put(TRANSACTION_TIME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TransactionTime = value;
                }
            });
            put(CURRENCY_EXPONENT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CurrencyExponent = value;
                }
            });
            put(CVV_PROMPT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CVVPrompt = value;
                }
            });

            put(ZIP_PROMPT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ZipCodePrompt = value;
                }
            });
            put(MERCHANT_CATEGORY_CODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MerchantCategoryCode = value;
                }
            });
            put(TRANSACTION_SEQUENCE_NUMBER, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TransactionSequenceNumber = value;
                }
            });
            put(LABEL1, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label1 = value;
                }
            });
            put(LABEL2, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label2 = value;
                }
            });
            put(LABEL3, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label3 = value;
                }
            });
            put(LABEL4, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label4 = value;
                }
            });
            put(LABEL1_PROPERTY, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label1Property = value;
                }
            });
            put(LABEL2_PROPERTY, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label2Property = value;
                }
            });
            put(LABEL3_PROPERTY, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label3Property = value;
                }
            });
            put(LABEL4_PROPERTY, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Label4Property = value;
                }
            });
            put(SIGNATURE_BOX, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.SignatureBox = value;
                }
            });
            put(BUTTON_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ButtonType = value;
                }
            });

            put(PADDING_CHAR, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PaddingChar = value;
                }
            });

            put(MAC_KEY_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MACKeyType = value;
                }
            });

            put(KSN_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.KSNFlag = value;
                }
            });

            put(TRACKDATA_SENTINEL, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TrackDataSentinel = value;
                }
            });

            put(CAMERA_SCAN_READER, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CameraScanReader = value;
                }
            });

            put(VAS_PROGRAM, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VASProgram = value;
                }
            });
            put(VAS_MODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.VASMode = value;
                }
            });
            put(VAS_SPECIAL_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    //UI Data was set in array list, So, we can judge vas program before set special data
                    Gson gson = new Gson();
                    if ("1".equals(request.VASProgram)) {
                        request.ApplePayVASData = gson.fromJson(value, ManageRequest.ApplePayVAS.class);
                    } else if ("2".equals(request.VASProgram)) {
                        request.GoogleSmartTapData = gson.fromJson(value, ManageRequest.GoogleSmartTap.class);
                    }
                }
            });
            put(VAS_SMART_TAP_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    Gson gson = new Gson();
                    request.GoogleSmartTapPushServiceData = gson.fromJson(value, ManageRequest.GoogleSmartTapPushService.class);
                }
            });

            put(TOKEN_COMMAND, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TokenCommand = value;
                }
            });

            put(TOKEN, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Token = value;
                }
            });

            put(TOKENSN, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TokenSN = value;
                }
            });

            put(EXPIRY_DATE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ExpiryDate = value;
                }
            });

            put(M1COMMAND, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.M1Command = value;
                }
            });

            put(BLOCKNO, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.BlockNo = value;
                }
            });

            put(ACCNT_CIPHER, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.Password = value;
                }
            });

            put(ACCNT_CIPHER_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PasswordType = value;
                }
            });

            put(BLOCK_VALUE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.BlockValue = value;
                }
            });

            put(UPDATEBLKNO, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.UpdateBlockNo = value;
                }
            });

            put(CONTINUOUS_SCREEN, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ContinuousScreen = value;
                }
            });
            put(PINPAD_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.PinpadType = value;
                }
            });
            put(ICON_NAME, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.IconName = value;
                }
            });
            put(SOURCE_KEY_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.SourceKeyType = value;
                }
            });
            put(SOURCE_KEY_INDEX, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.SourceKeyIndex = value;
                }
            });
            put(DESTINATION_KEY_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DestinationKeyType = value;
                }
            });
            put(DESTINATION_KEY_INDEX, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DestinationKeyIndex = value;
                }
            });
            put(DESTINATION_KEY_VALUE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DestinationKeyValue = value;
                }
            });
            put(CHECK_MODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CheckMode = value;
                }
            });
            put(CHECK_BUFFER, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.CheckBuffer = value;
                }
            });
            put(INPUT_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.InputData = value;
                }
            });
            put(ENCRYPTION_BITMAP, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EncryptionBitmap = value;
                }
            });
            put(MAC_KEY_SLOT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MACKeySlot = value;
                }
            });
            put(ENCRYPTION_KEY_SLOT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.EncryptionKeySlot = value;
                }
            });
            put(MAC_WORK_MODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.MACWorkMode = value;
                }
            });
            put(FALLBACK_INSERT_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.FallbackInsertEntryFlag = value;
                }
            });
            put(MULTI_MERCHANT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.MultiMerchant = gson.fromJson(value, MultiMerchant.class);
                    }
                }
            });
            put(KEY_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.KeyType = value;
                }
            });
            put(BARCODE_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.BarcodeType = value;
                }
            });
            put(BARCODE_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.BarcodeData = value;
                }
            });
            put(RECEIPT_PRINT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.ReceiptPrint = value;
                }
            });
            put(TRANSACTION_CVM_LIMIT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.TransactionCVMLimit = value;
                }
            });
            put(CUSTOM_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    String[] data = value.split("\\|");
                    request.CustomData = Arrays.asList(data);
                }
            });
            put(FALLBACK_MANUAL_ENTRY_FLAG, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.FallbackToManualEntryFlag = value;
                }
            });
            put(INPUT_TEXT_TITLE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.InputTextTitle = value;
                }
            });
            put(INPUT_TEXT, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.InputText = value;
                }
            });
            put(Custom_MAC_Information, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    Gson gson = new Gson();
                    request.CustomMACInformation = gson.fromJson(value, ManageRequest.CustomMACInformation.class);
                }
            });
            put(DATA_ENCRYPTION_TYPE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.DataEncryptionType = value;
                }
            });
            put(WORK_MODE, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.WorkMode = value;
                }
            });
            put(KEY_INDEX, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.KeyIndex = value;
                }
            });
            put(USER_DATA, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.UserData = value;
                }
            });
            put(INIT_VECTOR, new ManageItemValueSetter<String>() {
                @Override
                public void onSet(ManageRequest request, String value) {
                    request.InitVector = value;
                }
            });
        }
    };

    void onSet(ManageRequest request, T value);
}

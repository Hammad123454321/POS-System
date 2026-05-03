package com.pax.poslink.ui.manage;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.pax.poslink.entity.MultiMerchant;
import com.pax.poslink.main.MainConst;
import com.pax.poslink.model.MultiMerchantValueSetter;
import com.pax.poslink.model.payment.PaymentItemValueSetter;
import com.pax.poslink.ui.DirectoryLogActivity;
import com.pax.poslink.ui.FileManagerActivity;
import com.pax.poslink.MainApplication;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.R;
import com.pax.poslink.ui.MultiMerchantActivity;
import com.pax.poslink.ui.multicmd.MultiCmdViewModel;
import com.pax.poslink.ui.vas.VASSmartTapDataActivity;
import com.pax.poslink.ui.vas.VASSpecialDataActivity;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.model.manage.ManageItemValueSetter;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.ExtDataEntity;
import com.pax.poslink.view.NameStringWithUnitEntity;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.pax.poslink.util.Constant.BUNDLE_KEY_PAYMENT_MULTI_MERCHANT;
import static com.pax.poslink.util.Constant.PAYMENT_EXTDATA_RESULT;
import static com.pax.poslink.util.Constant.PAYMENT_MULTI_MERCHANT_RESULT;

public class ManageRequestFragment extends RequestFragment<ManageRequest> {

    private Spinner mTransTypeRequestEdit = null;
    private String specialDataJson = "";
    private String vasData = "";
    private String getVarMultiMerchantJson = "";
    private String setVarMultiMerchantJson = "";
    private String spinnerIndex = "";
    private String customMacDataJson = "";
    MultiCmdViewModel model;
    private ManageRequest manageRequest;

    public static final String INTENT_UPLOAD_IMAGE_PATH = "UploadImagePath";

    public ManageRequestFragment() {
        // Required empty public constructor
    }

    private final Map<String, List<RenderEntity>> commandMapRenderList = new HashMap<String, List<RenderEntity>>(){
        {

            List<String> edcTypes = MainConst.EDC_TYPES;
            List<String> transTypes = MainConst.TRANS_TYPE;
            manageRequest = new ManageRequest();
            NameValueStringEntity.ClickCallback browserClick = new NameValueStringEntity.ClickCallback() {
                @Override
                public void onClick(View v, final NameValueStringEntity entity) {
                    setActivityResultReceiver(new ActivityResultReceiver() {
                        @Override
                        public void onReceive(String data) {
                            entity.setValue(data);
                            UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                        }
                    });
                    String imgFile = entity.getValue();
                    File f = new File(imgFile);
                    if (!f.exists()) {
                        Toast.makeText(getActivity(), "no " + imgFile, Toast.LENGTH_LONG).show();
                        imgFile = Environment.getExternalStorageDirectory().toString();
                    } else {
                        if (f.isFile())
                            imgFile = imgFile.substring(0, imgFile.lastIndexOf("/"));
                    }
                    Intent intent2 = new Intent(getActivity(), FileManagerActivity.class);
                    Bundle bundle2 = new Bundle();
                    bundle2.putString(INTENT_UPLOAD_IMAGE_PATH, imgFile);
                    intent2.putExtras(bundle2);
                    startActivityForResult(intent2, Constant.MANAGE_UPLOAD_IMAGE_RESULT);
                }
            };
            NameValueStringEntity.ClickCallback sigfileClick = new NameValueStringEntity.ClickCallback() {
                @Override
                public void onClick(View v, final NameValueStringEntity entity) {
                    setActivityResultReceiver(new ActivityResultReceiver() {
                        @Override
                        public void onReceive(String data) {
                            entity.setValue(data);
                            UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                        }
                    });
                    String dir = entity.getValue();
                    File f1 = new File(dir);
                    if (!f1.exists()) {
                        dir = Environment.getExternalStorageDirectory().toString();
                    } else {
                        if (f1.isFile())
                            dir = dir.substring(0, dir.lastIndexOf("/"));
                    }
                    Intent intent3 = new Intent(getActivity(), DirectoryLogActivity.class);
                    Bundle bundle3 = new Bundle();
                    bundle3.putString("FilePath", dir);
                    intent3.putExtras(bundle3);
                    startActivityForResult(intent3, Constant.MANAGE_SAVE_IMAGE_RESULT);
                }
            };
            int transTypeIndex = 0;
            try {
                transTypeIndex = MainConst.TRANS_TYPE.indexOf(MainConst.slTrans[manageRequest.Trans]);
            } catch (Exception e) {
                //ignroe
            }
            //INIT
            put("INIT", null);
            //GETVAR
            put("GETVAR", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME, manageRequest.VarName, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME1, manageRequest.VarName1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME2, manageRequest.VarName2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME3, manageRequest.VarName3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME4, manageRequest.VarName4, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, ""),
                    new ButtonEntity(ManageItemValueSetter.MULTI_MERCHANT, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, final NameValueStringEntity entity) {
                            setActivityResultReceiver(new ActivityResultReceiver() {
                                @Override
                                public void onReceive(String data) {
                                    getVarMultiMerchantJson = data;
                                    entity.setValue(formatMultiMerchant(data));
                                    entity.setRealData(getVarMultiMerchantJson);
                                    UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                                }
                            });
                            Intent intent = new Intent(getActivity(), MultiMerchantActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(BUNDLE_KEY_PAYMENT_MULTI_MERCHANT, getVarMultiMerchantJson);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, PAYMENT_MULTI_MERCHANT_RESULT);
                        }
                    })
            ));
            //SETVAR
            put("SETVAR", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME, manageRequest.VarName, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE, manageRequest.VarValue, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME1, manageRequest.VarName1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE1, manageRequest.VarValue1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME2, manageRequest.VarName2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE2, manageRequest.VarValue2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME3, manageRequest.VarName3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE3, manageRequest.VarValue3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME4, manageRequest.VarName4, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE4, manageRequest.VarValue4, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, ""),
                    new ButtonEntity(ManageItemValueSetter.MULTI_MERCHANT, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, final NameValueStringEntity entity) {
                            setActivityResultReceiver(new ActivityResultReceiver() {
                                @Override
                                public void onReceive(String data) {
                                    setVarMultiMerchantJson = data;
                                    entity.setValue(formatMultiMerchant(data));
                                    entity.setRealData(setVarMultiMerchantJson);
                                    UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                                }
                            });
                            Intent intent = new Intent(getActivity(), MultiMerchantActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString(BUNDLE_KEY_PAYMENT_MULTI_MERCHANT, setVarMultiMerchantJson);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, PAYMENT_MULTI_MERCHANT_RESULT);
                        }
                    })
            ));
            //SHOWDIALOG
            put("SHOWDIALOG", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_1, manageRequest.Button1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_2, manageRequest.Button2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_3, manageRequest.Button3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_4, manageRequest.Button4, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1")
            ));
            //GETSIGNATURE
            put("GETSIGNATURE", Arrays.<RenderEntity>asList(
                    new NameValueBrowserEntity(ManageItemValueSetter.SAVE_SIG_PATH, "Browse", MainApplication.getInstance().getApplicationContext().getExternalFilesDir(null).getAbsolutePath(), InputType.TYPE_CLASS_TEXT, "", sigfileClick)
            ));
            //SHOWMESSAGE
            put("SHOWMESSAGE", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DISPLAY_MESSAGE, manageRequest.DisplayMessage, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DISPLAY_MESSAGE2, manageRequest.DisplayMessage2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOPDOWN, manageRequest.TopDown, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAX_LINE, manageRequest.TaxLine, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOTAL_LINE, manageRequest.TotalLine, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.IMAGE_NAME, manageRequest.ImageName, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.IMAGE_DESCRIPTION, manageRequest.ImageDescription, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LINE_ITEM_ACTION, manageRequest.LineItemAction, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ITEM_INDEX, manageRequest.ItemIndex, InputType.TYPE_CLASS_TEXT, "")
            ));
            //CLEARMESSAGE
            put("CLEARMESSAGE", null);
            //RESET
            put("RESET", null);
            //UPDATERESOURCE
            put("UPDATERESOURCE", Arrays.<RenderEntity>asList(
                    new NameValueBrowserEntity(ManageItemValueSetter.FILE_PATH, "Browse", Environment.getExternalStorageDirectory().getAbsolutePath(), InputType.TYPE_CLASS_TEXT, "", browserClick),
                    new NameValueStringEntity(ManageItemValueSetter.FILE_TYPE, StringUtil.isEmpty(manageRequest.FileType) ? "0" : manageRequest.FileType, InputType.TYPE_CLASS_NUMBER, "Default 0"),
                    new NameValueStringEntity(ManageItemValueSetter.TARGET_DEVICE, StringUtil.isEmpty(manageRequest.TargetDevice) ? "0" : manageRequest.TargetDevice, InputType.TYPE_CLASS_NUMBER, "Default 0"),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT,
                            StringUtil.isEmpty(manageRequest.TimeOut) ? "1200" : manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, "")
            ));
            //DOSIGNATURE
            put("DOSIGNATURE", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueStringEntity(ManageItemValueSetter.UPLOAD, manageRequest.Upload + "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.H_REF_NUM, manageRequest.HRefNum, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, StringUtil.isEmpty(manageRequest.TimeOut) ? "600" : manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1")
            ));
            //DELETEIMAGE
            put("DELETEIMAGE", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.IMAGE_NAME, manageRequest.ImageName, InputType.TYPE_CLASS_TEXT, "")
            ));
            //SHOWMESSAGECENTER
            put("SHOWMESSAGECENTER", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.THANK_YOU_TITLE, manageRequest.ThankYouTitle, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.THANK_YOU_MESSAGE1, manageRequest.ThankYouMessage1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.THANK_YOU_MESSAGE2, manageRequest.ThankYouMessage2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.THANK_YOU_TIME_OUT, manageRequest.ThankYouTimeOut, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PINPAD_TYPE, StringUtil.isEmpty(manageRequest.PinpadType) ? "0" : manageRequest.PinpadType, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ICON_NAME, StringUtil.isEmpty(manageRequest.IconName) ? "" : manageRequest.IconName, InputType.TYPE_CLASS_TEXT, "")

            ));
            //REBOOT
            put("REBOOT", null);
            //GETPINBLOCK
            put("GETPINBLOCK", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueSelectEntity(ManageItemValueSetter.TRANS_TYPE, "", transTypes, transTypes, transTypeIndex),
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ACCOUNT_NUMBER, StringUtil.isEmpty(manageRequest.AccountNumber) ? "5454545454545454" : manageRequest.AccountNumber, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_TYPE, StringUtil.isEmpty(manageRequest.EncryptionType) ? "1" : manageRequest.EncryptionType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, StringUtil.isEmpty(manageRequest.KeySlot) ? "1" : manageRequest.KeySlot, InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MIN_LENGTH, StringUtil.isEmpty(manageRequest.PinMinLength) ? "4" : manageRequest.PinMinLength, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MAX_LENGTH, StringUtil.isEmpty(manageRequest.PinMaxLength) ? "12" : manageRequest.PinMaxLength, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.NULL_PIN_FLAG, StringUtil.isEmpty(manageRequest.NullPin) ? "1" : manageRequest.NullPin, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_ALGORITHM, StringUtil.isEmpty(manageRequest.PinAlgorithm) ? "1" : manageRequest.PinAlgorithm, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, StringUtil.isEmpty(manageRequest.TimeOut) ? "300" : manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PINPAD_TYPE, StringUtil.isEmpty(manageRequest.PinpadType) ? "0" : manageRequest.PinpadType, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KSN_FLAG, StringUtil.isEmpty(manageRequest.KSNFlag) ? "0" : manageRequest.KSNFlag, InputType.TYPE_CLASS_TEXT, "")
            ));
            //INPUTACCOUNT
            put("INPUTACCOUNT", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueSelectEntity(ManageItemValueSetter.TRANS_TYPE, "", transTypes, transTypes, transTypeIndex),
                    new NameValueStringEntity(ManageItemValueSetter.MAGNETIC_SWIPE_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.MagneticSwipeEntryFlag) ? "1" : manageRequest.MagneticSwipeEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MANUAL_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.ManualEntryFlag) ? "1" : manageRequest.ManualEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTACTLESS_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.ContactlessEntryFlag) ? "1" : manageRequest.ContactlessEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.SCANNER_ENTRY_FLAG, manageRequest.ScannerEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXPIRY_DATE_PROMPT, StringUtil.isEmpty(manageRequest.ExpiryDatePrompt) ? "1" : manageRequest.ExpiryDatePrompt, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_FLAG, manageRequest.EncryptionFlag, InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, StringUtil.isEmpty(manageRequest.KeySlot) ? "1" : manageRequest.KeySlot, InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.MIN_ACCOUNT_LENGTH, manageRequest.MINAccountLength, InputType.TYPE_CLASS_TEXT, "default 10"),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_ACCOUNT_LENGTH, manageRequest.MAXAccountLength, InputType.TYPE_CLASS_TEXT, "default 19"),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1")
            ));
            //RESETMSR
            put("RESETMSR", null);
            //INPUTTEXT
            put("INPUTTEXT", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TYPE, manageRequest.InputType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MIN_LENGTH, manageRequest.MINLength, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_LENGTH, manageRequest.MAXLength, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DEFAULT_VALUE, manageRequest.DefaultValue, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1")
            ));
            //CHECKFILE
            put("CHECKFILE", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.FILE_NAME, manageRequest.FileName, InputType.TYPE_CLASS_TEXT, "")
            ));
            //AUTHORIZECARD
            put("AUTHORIZECARD", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.AMOUNT, StringUtil.isEmpty(manageRequest.Amount) ? "100" : manageRequest.Amount, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CASH_BACK_AMOUNT, manageRequest.CashBackAmt, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MERCHANT_DECISION, StringUtil.isEmpty(manageRequest.MerchantDecision) ? "0" : manageRequest.MerchantDecision, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_TYPE, StringUtil.isEmpty(manageRequest.EncryptionType) ? "1" : manageRequest.EncryptionType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, StringUtil.isEmpty(manageRequest.KeySlot) ? "1" : manageRequest.KeySlot, InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MIN_LENGTH, StringUtil.isEmpty(manageRequest.PinMinLength) ? "4" : manageRequest.PinMinLength, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MAX_LENGTH, StringUtil.isEmpty(manageRequest.PinMaxLength) ? "12" : manageRequest.PinMaxLength, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_BYPASS, manageRequest.PINBypass, InputType.TYPE_CLASS_TEXT, "pin bypass"),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_ALGORITHM, StringUtil.isEmpty(manageRequest.PinAlgorithm) ? "1" : manageRequest.PinAlgorithm, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EMV_KERNEL_CONFIGURATION_SELECTION, manageRequest.EmvKernelConfigurationSelection, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_DATE, manageRequest.TransactionDate, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_TIME, manageRequest.TransactionTime, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_CODE, manageRequest.CurrencyCode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_EXPONENT, manageRequest.CurrencyExponent, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAG_LIST, manageRequest.TagList, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, StringUtil.isEmpty(manageRequest.TimeOut) ? "300" : manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PINPAD_TYPE, StringUtil.isEmpty(manageRequest.PinpadType) ? "0" : manageRequest.PinpadType, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KSN_FLAG, StringUtil.isEmpty(manageRequest.KSNFlag) ? "0" : manageRequest.KSNFlag, InputType.TYPE_CLASS_TEXT, "")
            ));
            //COMPLETEONLINEEMV
            put("COMPLETEONLINEEMV", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.ONLINE_AUTH_RESULT, manageRequest.OnlineAuthorizationResult, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.RESPONSE_CODE, manageRequest.ResponseCode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.AUTHORIZATION_CODE, manageRequest.AuthorizationCode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_AUTH_DATA, manageRequest.IssuerAuthenticationData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_SCRIPT_1, manageRequest.IssuerScript1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_SCRIPT_2, manageRequest.IssuerScript2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, "")
            ));
            //REMOVECARD
            put("REMOVECARD", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.MESSAGE_1, manageRequest.Message1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MESSAGE_2, manageRequest.Message2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1"),
                    new NameValueStringEntity(ManageItemValueSetter.PINPAD_TYPE, StringUtil.isEmpty(manageRequest.PinpadType) ? "0" : manageRequest.PinpadType, InputType.TYPE_CLASS_NUMBER, "")
            ));
            //GETEMVTLVDATA
            put("GETEMVTLVDATA", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TAG_LIST, manageRequest.TagList, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TLV_TYPE, manageRequest.TLVType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, "")
            ));
            //SETEMVTLVDATA
            put("SETEMVTLVDATA", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.EMV_DATA, manageRequest.EMVData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TLV_TYPE, manageRequest.TLVType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, "")
            ));
            //INPUTACCOUNTWITHEMV
            put("INPUTACCOUNTWITHEMV", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueSelectEntity(ManageItemValueSetter.TRANS_TYPE, "", transTypes, transTypes, transTypeIndex),
                    new NameValueStringEntity(ManageItemValueSetter.AMOUNT, StringUtil.isEmpty(manageRequest.Amount) ? "100" : manageRequest.Amount, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CASH_BACK_AMOUNT, manageRequest.CashBackAmt, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAGNETIC_SWIPE_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.MagneticSwipeEntryFlag) ? "1" : manageRequest.MagneticSwipeEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MANUAL_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.ManualEntryFlag) ? "1" : manageRequest.ManualEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTACTLESS_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.ContactlessEntryFlag) ? "1" : manageRequest.ContactlessEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTACT_EMV_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.ContactEMVEntryFlag) ? "1" : manageRequest.ContactEMVEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.FALLBACK_SWIPE_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.FallbackSwipeEntryFlag) ? "1" : manageRequest.FallbackSwipeEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXPIRY_DATE_PROMPT, StringUtil.isEmpty(manageRequest.ExpiryDatePrompt) ? "1" : manageRequest.ExpiryDatePrompt, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CVV_PROMPT, StringUtil.isEmpty(manageRequest.CVVPrompt) ? "1" : manageRequest.CVVPrompt, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ZIP_PROMPT, StringUtil.isEmpty(manageRequest.ZipCodePrompt) ? "1" : manageRequest.ZipCodePrompt, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_FLAG, manageRequest.EncryptionFlag, InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, StringUtil.isEmpty(manageRequest.KeySlot) ? "1" : manageRequest.KeySlot, InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.PADDING_CHAR, manageRequest.PaddingChar, InputType.TYPE_CLASS_TEXT, "default is 0"),
                    new NameValueStringEntity(ManageItemValueSetter.TRACKDATA_SENTINEL, StringUtil.isEmpty(manageRequest.TrackDataSentinel) ? "1" : manageRequest.TrackDataSentinel, InputType.TYPE_CLASS_TEXT, "1: need to include start and end sentinel. Default is 1."),
                    new NameValueStringEntity(ManageItemValueSetter.MIN_ACCOUNT_LENGTH, manageRequest.MINAccountLength, InputType.TYPE_CLASS_TEXT, "default 10"),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_ACCOUNT_LENGTH, manageRequest.MAXAccountLength, InputType.TYPE_CLASS_TEXT, "default 19"),
                    new NameValueStringEntity(ManageItemValueSetter.EMV_KERNEL_CONFIGURATION_SELECTION, manageRequest.EmvKernelConfigurationSelection, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_DATE, manageRequest.TransactionDate, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_TIME, manageRequest.TransactionTime, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_CODE, StringUtil.isEmpty(manageRequest.CurrencyCode) ? "840" : manageRequest.CurrencyCode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_EXPONENT, manageRequest.CurrencyExponent, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MERCHANT_CATEGORY_CODE, manageRequest.MerchantCategoryCode, InputType.TYPE_CLASS_TEXT, "4 digits"),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_SEQUENCE_NUMBER, manageRequest.TransactionSequenceNumber, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAG_LIST, manageRequest.TagList, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1"),
                    new NameValueStringEntity(ManageItemValueSetter.FALLBACK_INSERT_ENTRY_FLAG, manageRequest.FallbackInsertEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_CVM_LIMIT, manageRequest.TransactionCVMLimit, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, StringUtil.isEmpty(manageRequest.TimeOut) ? "300" : manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KSN_FLAG, StringUtil.isEmpty(manageRequest.KSNFlag) ? "0" : manageRequest.KSNFlag, InputType.TYPE_CLASS_TEXT, ""),
//                    new NameValueStringEntity(ManageItemValueSetter.ENABLE_LUHN_CHECK, "0", InputType.TYPE_CLASS_TEXT, ""),
                    new ExtDataEntity(PaymentItemValueSetter.EXT_DATA, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, final NameValueStringEntity entity) {
                            setActivityResultReceiver(new ActivityResultReceiver() {
                                @Override
                                public void onReceive(String data) {
                                    entity.setValue(data);
                                    UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                                }
                            });

                            Intent intent = new Intent(getActivity(), ManageExtDataActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("extdata", entity.getValue());
                            intent.putExtras(bundle);
                            startActivityForResult(intent, PAYMENT_EXTDATA_RESULT);
                        }
                    }),
                    new NameValueStringEntity(ManageItemValueSetter.CUSTOM_DATA, "", InputType.TYPE_CLASS_TEXT, "Split strs by |, eg. str0|str1"),
                    new NameValueStringEntity(ManageItemValueSetter.FALLBACK_MANUAL_ENTRY_FLAG, StringUtil.isEmpty(manageRequest.FallbackToManualEntryFlag) ? "0" : manageRequest.FallbackToManualEntryFlag, InputType.TYPE_CLASS_TEXT, ""),
                    new ButtonEntity(ManageItemValueSetter.Custom_MAC_Information, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, final NameValueStringEntity entity) {
                            setActivityResultReceiver(new ActivityResultReceiver() {
                                @Override
                                public void onReceive(String data) {
                                    entity.setValue(formatSpecialData(data));
                                    customMacDataJson = data;
                                    UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                                }
                            });
                            Intent intent = new Intent(getContext(), CustomMACInformationActivity.class);
                            intent.putExtra("custom_mac_information", customMacDataJson);
                            startActivityForResult(intent, Constant.MANAGE_HMAC_INFORMATION);
                        }
                    })

            ));
            //COMPLETECONTACTLESSEMV
            put("COMPLETECONTACTLESSEMV", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_AUTH_DATA, manageRequest.IssuerAuthenticationData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_SCRIPT_1, manageRequest.IssuerScript1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_SCRIPT_2, manageRequest.IssuerScript2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXT_DATA, manageRequest.ExtData, InputType.TYPE_CLASS_TEXT, "")
            ));
            //SETSAFPARAMETERS
            put("SETSAFPARAMETERS", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.SAF_MODE, manageRequest.SAFMode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.START_DATE_TIME, manageRequest.StartDateTime, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.END_DATE_TIME, manageRequest.EndDateTime, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DURATION_IN_DAYS, manageRequest.DurationInDays, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_NUMBER, manageRequest.MaxNumber, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOTAL_CEILING_AMOUNT, manageRequest.TotalCeilingAmount, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CEILING_AMOUNT_PER_CARD_TYPE, manageRequest.CeilingAmountPerCardType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.HALO_PER_CARD_TYPE, manageRequest.HALOPerCardType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.SAF_UPLOAD_MODE, manageRequest.SAFUploadMode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.AUTO_UPLOAD_INTERVAL_TIME, manageRequest.AutoUploadIntervalTime, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DELETE_SAF_CONFIRMATION, manageRequest.DeleteSAFConfirmation, InputType.TYPE_CLASS_TEXT, "")
            ));
            //SHOWTEXTBOX
            put("SHOWTEXTBOX", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TEXT, manageRequest.Text, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_1, manageRequest.Button1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_COLOR1, manageRequest.ButtonColor1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_2, manageRequest.Button2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_COLOR2, manageRequest.ButtonColor2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_3, manageRequest.Button3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_COLOR3, manageRequest.ButtonColor3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_KEY1, manageRequest.ButtonKey1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_KEY2, manageRequest.ButtonKey2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_KEY3, manageRequest.ButtonKey3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENABLE_HARD_KEY, manageRequest.EnableHardKey, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.HARD_KEY_LIST, manageRequest.HardKeyList, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.SIGNATURE_BOX, manageRequest.SignatureBox, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueBrowserEntity(ManageItemValueSetter.SAVE_SIG_PATH, "Browse", MainApplication.getInstance().getApplicationContext().getExternalFilesDir(null).getAbsolutePath(), InputType.TYPE_CLASS_TEXT, "", sigfileClick),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1"),
                    new NameValueStringEntity(ManageItemValueSetter.BARCODE_TYPE, StringUtil.isEmpty(manageRequest.BarcodeType) ? "7" : manageRequest.BarcodeType, InputType.TYPE_CLASS_NUMBER, "Empty or 7(QRCode)"),
                    new NameValueStringEntity(ManageItemValueSetter.BARCODE_DATA, manageRequest.BarcodeData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TEXT_TITLE, manageRequest.InputTextTitle, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TEXT, manageRequest.InputText, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_TYPE, manageRequest.InputType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MIN_LENGTH, manageRequest.MINLength, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_LENGTH, manageRequest.MAXLength, InputType.TYPE_CLASS_TEXT, "")
            ));
            //REPRINT
            put("REPRINT", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueStringEntity(ManageItemValueSetter.LAST_RECEIPT, manageRequest.LastReceipt, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.REF_NUM, manageRequest.RefNum, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.AUTHORIZATION_CODE, manageRequest.AuthorizationCode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ECR_REF_NUM, manageRequest.ECRRefNum, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.RECEIPT_PRINT, manageRequest.ReceiptPrint, InputType.TYPE_CLASS_TEXT, "")
            ));
            //PRINTER
            put("PRINTER", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.PRINT_COPY, manageRequest.PrintCopy, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PRINT_DATA, StringUtil.isEmpty(manageRequest.PrintData) ? "\\$Header\\I\\1\\Cinvert\n" +
                            "\\I\\Rinvert\n" +
                            "\\Iinvert\n" +
                            "\\Lleft\\Ccenter\\R\\2right\n" +
                            "\\Lleft\\Ccenter\\R\\2right\n" +
                            "\\L\\3left\\3\\Ccenter\\R\\3right\n" +
                            "\\L\\3left\\3\\Ccenter\\R\\3right\n" +
                            "\\L\\1left\\C\\1center\\1\\Rright\n" +
                            "\\L\\1left\\C\\1center\\1\\Rright\n" +
                            "\\$Date\n" +
                            "\\C\\$Time\n" +
                            "\\3\\$SN#\n" +
                            "\\$BARD,1,1,8,12345678\n" +
                            "\\C\\$Time\n" +
                            "\\$BARD,2,1,8,56781234\n" +
                            "\\C\\$Time\n" +
                            "\\$BARD,3,1,12,123456789012\n" +
                            "\\C\\$Time\n" +
                            "\\$BARD,4,1,12,[3102]000035\n" +
                            "\\C\\$Time\n" +
                            "\\$BARD,5,2,8,57682143\n" +
                            "\\C\\$Time\n" +
                            "\\$BARD,7,8,27,56781234abcd111243232123fds\n" +
                            "\\C\\$Time\n" +
                            "\\$Disclaimer\\$Trailer" : manageRequest.PrintData, InputType.TYPE_CLASS_TEXT, "")
            ));
            //SHOWITEM
            put("SHOWITEM", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOPDOWN, manageRequest.TopDown, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAX_LINE, manageRequest.TaxLine, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOTAL_LINE, manageRequest.TotalLine, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ITEMS_PASSTHRUDATA, StringUtil.isEmpty(manageRequest.ItemData) ? "Fruit,,3,0,,4,,," : manageRequest.ItemData, InputType.TYPE_CLASS_TEXT, "Fruit,,3,0,,4,,,"),
                    new NameValueStringEntity(ManageItemValueSetter.LINE_ITEM_ACTION, manageRequest.LineItemAction, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ITEM_INDEX, manageRequest.ItemIndex, InputType.TYPE_CLASS_TEXT, "")
            ));
            //CARDINSERTDETECTION
            put("CARDINSERTDETECTION", null);
            //TOKENADMINISTRATIVE
            put("TOKENADMINISTRATIVE", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, "", edcTypes, edcTypes, manageRequest.EDCType),
                    new NameValueStringEntity(ManageItemValueSetter.TOKEN_COMMAND, manageRequest.TokenCommand, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOKEN, manageRequest.Token, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TOKENSN, manageRequest.TokenSN, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXPIRY_DATE, manageRequest.ExpiryDate, InputType.TYPE_CLASS_TEXT, "")
            ));
            //SHOWDIALOGFORM
            put("SHOWDIALOGFORM", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, manageRequest.Title, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL1, manageRequest.Label1, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL1_PROPERTY, manageRequest.Label1Property, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL2, manageRequest.Label2, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL2_PROPERTY, manageRequest.Label2Property, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL3, manageRequest.Label3, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL3_PROPERTY, manageRequest.Label3Property, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL4, manageRequest.Label4, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.LABEL4_PROPERTY, manageRequest.Label4Property, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BUTTON_TYPE, manageRequest.ButtonType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, StringUtil.isEmpty(manageRequest.TimeOut) ? "300" : manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, StringUtil.isEmpty(manageRequest.ContinuousScreen) ? "0" : manageRequest.ContinuousScreen, InputType.TYPE_CLASS_NUMBER, "Default 0, valid when set 1")
            ));
            //CAMERASCAN
            put("CAMERASCAN", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.CAMERA_SCAN_READER, StringUtil.isEmpty(manageRequest.CameraScanReader) ? "1" : manageRequest.CameraScanReader, InputType.TYPE_CLASS_TEXT, "0:Rear, 1:Front"),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, StringUtil.isEmpty(manageRequest.TimeOut) ? "300" : manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, "")
            ));
            final NameValueSelectEntity vasProgramEntity =  new NameValueSelectEntity(ManageItemValueSetter.VAS_PROGRAM, "1",
                    Arrays.asList(MainApplication.getInstance().getResources().getStringArray(R.array.VAS_PROGRAMS)),
                    Arrays.asList("1", "2"));
            //VASSETMERCHANTPARAMETERS
            put("VASSETMERCHANTPARAMETERS", Arrays.<RenderEntity>asList(
                    vasProgramEntity,
                    new NameValueSelectEntity(ManageItemValueSetter.VAS_MODE, "0",
                            Arrays.asList(MainApplication.getInstance().getResources().getStringArray(R.array.VAS_MODE)),
                            Arrays.asList("0", "1", "2", "3")),

                    new ButtonEntity(ManageItemValueSetter.VAS_SPECIAL_DATA, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, final NameValueStringEntity entity) {
                            setActivityResultReceiver(new ActivityResultReceiver() {
                                @Override
                                public void onReceive(String data) {
                                    entity.setValue(formatSpecialData(data));
                                    specialDataJson = data;
                                    UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                                }
                            });
                            Intent intent = new Intent(getContext(), VASSpecialDataActivity.class);
                            intent.putExtra("vas_program", String.valueOf(vasProgramEntity.getValue()));
                            intent.putExtra("vas_specialData", specialDataJson);
                            startActivityForResult(intent, Constant.MANAGE_VAS_SPECIAL_DATA_RESULT);
                        }
                    })
            ));
            //VASPUSHDATA
            put("VASPUSHDATA", Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.VAS_MODE, "0",
                            Arrays.asList(MainApplication.getInstance().getResources().getStringArray(R.array.VAS_MODE)),
                            Arrays.asList("0", "1", "2", "3")),

                    new ButtonEntity(ManageItemValueSetter.VAS_SMART_TAP_DATA, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, final NameValueStringEntity entity) {
                            setActivityResultReceiver(new ActivityResultReceiver() {
                                @Override
                                public void onReceive(String data) {
                                    entity.setValue(formatSpecialData(data));
                                    vasData = data;
                                    UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                                }
                            });
                            Intent intent = new Intent(getContext(), VASSmartTapDataActivity.class);
                            intent.putExtra("vasData", vasData);
                            startActivityForResult(intent, Constant.MANAGE_VAS_SPECIAL_DATA_RESULT);
                        }
                    })
            ));
            //MIFARECARD
            put("MIFARECARD", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.M1COMMAND, manageRequest.M1Command, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BLOCKNO, manageRequest.BlockNo, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ACCNT_CIPHER, manageRequest.Password, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ACCNT_CIPHER_TYPE, manageRequest.PasswordType, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.BLOCK_VALUE, manageRequest.BlockValue, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.UPDATEBLKNO, manageRequest.UpdateBlockNo, InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, manageRequest.TimeOut, InputType.TYPE_CLASS_TEXT, "")
            ));
            //GETSAFPARAMETERS
            put("GETSAFPARAMETERS", Arrays.<RenderEntity>asList(
            ));
            //SESSIONKEYINJECTION
            put("SESSIONKEYINJECTION", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.SOURCE_KEY_TYPE, manageRequest.SourceKeyType, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.SOURCE_KEY_INDEX, manageRequest.SourceKeyIndex, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DESTINATION_KEY_TYPE, manageRequest.DestinationKeyType, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DESTINATION_KEY_INDEX, manageRequest.DestinationKeyIndex, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.DESTINATION_KEY_VALUE, manageRequest.DestinationKeyValue, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CHECK_MODE, manageRequest.CheckMode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CHECK_BUFFER, manageRequest.CheckBuffer, InputType.TYPE_CLASS_TEXT, "")
            ));
            //MACCALCULATION
            put("MACCALCULATION", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.INPUT_DATA, manageRequest.InputData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_BITMAP, manageRequest.EncryptionBitmap, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAC_KEY_SLOT, manageRequest.MACKeySlot, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAC_WORK_MODE, manageRequest.MACWorkMode, InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_KEY_SLOT, manageRequest.EncryptionKeySlot, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PADDING_CHAR, manageRequest.PaddingChar, InputType.TYPE_CLASS_TEXT, "default is 0"),
                    new NameValueStringEntity(ManageItemValueSetter.MAC_KEY_TYPE, manageRequest.MACKeyType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KSN_FLAG, StringUtil.isEmpty(manageRequest.KSNFlag) ? "0" : manageRequest.KSNFlag, InputType.TYPE_CLASS_TEXT, "")
            ));
            //GETPEDINFORMATION
            put("GETPEDINFORMATION", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.KEY_TYPE, manageRequest.KeyType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, StringUtil.isEmpty(manageRequest.KeySlot) ? "1" : manageRequest.KeySlot, InputType.TYPE_CLASS_TEXT, "Inject key first")
            ));
            //INCREASEKSN
            put("INCREASEKSN", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.KEY_TYPE, manageRequest.KeyType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, StringUtil.isEmpty(manageRequest.KeySlot) ? "1" : manageRequest.KeySlot, InputType.TYPE_CLASS_TEXT, "Inject key first")
            ));
            put("CUSTOMDATAENCRYPTION", Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.DATA_ENCRYPTION_TYPE, manageRequest.DataEncryptionType, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.WORK_MODE, manageRequest.WorkMode, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_INDEX, manageRequest.KeyIndex, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.USER_DATA, manageRequest.UserData, InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.INIT_VECTOR, manageRequest.InitVector, InputType.TYPE_CLASS_TEXT, "")
            ));
        }

    };

    public static ManageRequestFragment newInstance() {
        return new ManageRequestFragment();
    }

    public static ManageRequestFragment newInstance(String processBtnName) {
        ManageRequestFragment fragment = new ManageRequestFragment();
        fragment.setProcessBtn(processBtnName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_manage_request;
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    @Override
    public void initView(View view) {
        final String[] strArrayTrans = getResources().getStringArray(R.array.manage_trans);
        requestContainer = view.findViewById(R.id.manage_request_container);
        mTransTypeRequestEdit = view.findViewById(R.id.manage_request_transType);
        List<String> list = Arrays.asList(strArrayTrans);
        model = new ViewModelProvider(requireActivity()).get(MultiCmdViewModel.class);
        manageRequest = model.getManageRequest();
        try {
            spinnerIndex = MainConst.slManageTrans[manageRequest.TransType - 1];
        } catch (Exception e) {
            //ignore
        }
        mTransTypeRequestEdit.setSelection(!list.contains(spinnerIndex) ? 0 : list.indexOf(spinnerIndex));
        mTransTypeRequestEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String command = strArrayTrans[position];
                showCorrespondingRequestView(command);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showCorrespondingRequestView(String command) {
        requestRenderEntityList.clear();
        requestContainer.removeAllViews();
        List<RenderEntity> commandRenderList = commandMapRenderList.get(command);
        if (commandRenderList != null) {
            requestRenderEntityList.addAll(commandRenderList);
        }
        for (RenderEntity renderEntity : requestRenderEntityList) {
            String name = ((NameValueEntity) renderEntity).getName();
            CommonItemView itemView = renderEntity.createView(requestContainer);
            requestContainer.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.MANAGE_UPLOAD_IMAGE_RESULT:
                if (resultCode == RESULT_OK) {
                    if (activityResultReceiver != null) {
                        activityResultReceiver.onReceive(data.getStringExtra(INTENT_UPLOAD_IMAGE_PATH));
                    }

                }
                break;

            case Constant.MANAGE_SAVE_IMAGE_RESULT:
                if (resultCode == RESULT_OK) {
                    if (activityResultReceiver != null) {
                        activityResultReceiver.onReceive(data.getStringExtra("FilePath"));
                    }
                }
                break;
            case Constant.MANAGE_VAS_SPECIAL_DATA_RESULT:
                if (resultCode == RESULT_OK) {
                    if (activityResultReceiver != null) {
                        activityResultReceiver.onReceive(data.getStringExtra("vas_special"));
                    }
                }
                break;
            case Constant.MANAGE_HMAC_INFORMATION:
                if (resultCode == RESULT_OK) {
                    if (activityResultReceiver != null) {
                        activityResultReceiver.onReceive(data.getStringExtra("custom_mac_information"));
                    }
                }
                break;
            case PAYMENT_MULTI_MERCHANT_RESULT:
                switch (resultCode) {
                    case RESULT_OK:
                        onActivityResultReceive(data.getStringExtra(BUNDLE_KEY_PAYMENT_MULTI_MERCHANT));
                        break;
                    default:
                        break;
                }
                break;

            case PAYMENT_EXTDATA_RESULT:
                if (resultCode == RESULT_OK) {
                    if (activityResultReceiver != null) {
                        activityResultReceiver.onReceive(data.getStringExtra("extdata"));
                    }
                }
                break;

            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public ManageRequest getRequest() {
        ManageRequest mgrequest = new ManageRequest();
        setManageRequest(mgrequest);
        return mgrequest;
    }

    private void setManageRequest(ManageRequest request) {
        String TransType = mTransTypeRequestEdit.getSelectedItem().toString();
        request.TransType = request.ParseTransType(TransType);
        for (RenderEntity renderEntity : requestRenderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                ManageItemValueSetter valueSetter = ManageItemValueSetter.VALUE_SETTER_MAP.get(entity.getName());
                valueSetter.onSet(request, entity.getItemValues().get(entity.getSelectedItem()));

            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                ManageItemValueSetter valueSetter = ManageItemValueSetter.VALUE_SETTER_MAP.get(nameValueStringEntity.getName());
                if (ManageItemValueSetter.VAS_SPECIAL_DATA.equals(nameValueStringEntity.getName())) {
                    valueSetter.onSet(request, specialDataJson);
                } else if (ManageItemValueSetter.VAS_SMART_TAP_DATA.equals(nameValueStringEntity.getName())) {
                    valueSetter.onSet(request, vasData);
                }else if(ManageItemValueSetter.Custom_MAC_Information.equals(nameValueStringEntity.getName())){
                    valueSetter.onSet(request, customMacDataJson);
                } else {
                    if (!TextUtils.isEmpty(nameValueStringEntity.getRealData())) {
                        valueSetter.onSet(request, nameValueStringEntity.getRealData());
                    } else {
                        valueSetter.onSet(request, nameValueStringEntity.getValue());
                    }
//                    valueSetter.onSet(request, nameValueStringEntity.getValue());
                }
            }
        }
    }

    public String getSelectedRequest() {
        return mTransTypeRequestEdit.getSelectedItem().toString();
    }

    private String formatSpecialData(String commercialJson) {
        String result = "";
        try {
            JSONObject jsonObject = new JSONObject(commercialJson);
            Iterator it = jsonObject.keys();
            String key;
            while(it.hasNext()) {
                key = (String) it.next().toString();
                String vol = jsonObject.getString(key);
                result += toFormat(key, vol);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String toFormat(String name, String value) {
        String result = "";
        if (TextUtils.isEmpty(value))
            return result;
        result += name + ": " + value + "\n";
        return result;
    }

    private String formatMultiMerchant(String fleetCardJson) {
        String result = "";
        Gson gson = new Gson();
        MultiMerchant multiMerchant = gson.fromJson(fleetCardJson, MultiMerchant.class);
        result += toFormat(MultiMerchantValueSetter.MM_ID, multiMerchant.Id);
        result += toFormat(MultiMerchantValueSetter.MM_NAME, multiMerchant.Name);

        return result;
    }
}

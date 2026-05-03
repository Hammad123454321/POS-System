package com.pax.poslink.fullIntegration;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.pax.poslink.MainApplication;
import com.pax.poslink.R;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.fullIntegration.mock.MockRequest;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.main.MainConst;
import com.pax.poslink.model.manage.ManageItemValueSetter;
import com.pax.poslink.ui.FileManagerActivity;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.ui.manage.CustomMACInformationActivity;
import com.pax.poslink.ui.manage.ManageFragment;
import com.pax.poslink.ui.vas.VASSmartTapDataActivity;
import com.pax.poslink.ui.vas.VASSpecialDataActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.LogStaticWrapper;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.sharepref.SharedPrefKey;
import com.pax.poslink.util.sharepref.SharedPreferenceHelper;
import com.pax.poslink.view.ButtonEntity;
import com.pax.poslink.view.NameStringWithUnitEntity;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.widget.EnterPINDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FullIntegrateRequestFragment extends RequestFragment {

    private static final String CMD_INPUTACCOUNT_EMV = "INPUTACCOUNTWITHEMV";
    private static final String CMD_SET_EMV_TLV_DATA = "SETEMVTLVDATA";
    private static final String CMD_GET_EMV_TLV_DATA = "GETEMVTLVDATA";
    private static final String CMD_REMOVE_CARD = "REMOVECARD";
    private static final String CMD_COMPLETE_ONLINE_EMV = "COMPLETEONLINEEMV";
    private static final String CMD_MOCK_TRANSACTION = "Mock_Transaction";
    private static final String CMD_AUTHORIZECARD = "AUTHORIZECARD";
    private static final String CMD_GET_PIN_BLOCK = "GETPINBLOCK";
    private static final String CMD_UPDATE_RESOURCE = "UPDATERESOURCE";
    private static final String CMD_CHECK_FILE = "CHECKFILE";
    private static final String CMD_INIT = "INIT";
    private static final String CMD_SETVAR = "SETVAR";
    private static final String CMD_GETVAR = "GETVAR";
    private static final String CMD_VAS_SET_MERCHANT_PARAMETERS = "VASSETMERCHANTPARAMETERS";
    private static final String CMD_VAS_PUSH_DATA = "VASPUSHDATA";

    private Spinner cmdEdit = null;

    private String specialDataJson = "";
    private String vasData = "";
    private String customMacDataJson = "";

    private static final List<String> CMDS = new ArrayList<String>() {
        {
            addAll(Arrays.asList(
                    CMD_INPUTACCOUNT_EMV, CMD_AUTHORIZECARD, CMD_SET_EMV_TLV_DATA, CMD_GET_EMV_TLV_DATA,
                    CMD_COMPLETE_ONLINE_EMV, CMD_REMOVE_CARD, CMD_GET_PIN_BLOCK, CMD_UPDATE_RESOURCE, CMD_CHECK_FILE,
                    CMD_INIT, CMD_SETVAR, CMD_GETVAR, CMD_VAS_SET_MERCHANT_PARAMETERS, CMD_VAS_PUSH_DATA));

            if (Convenience.isButtonClickEnough()) {
                add(CMD_MOCK_TRANSACTION);
            }
        }
    };

    private final Map<String, List<RenderEntity>> commandMapRenderList = new HashMap<String, List<RenderEntity>>() {
        {
            List<String> edcTypes = MainConst.EDC_TYPES;
            List<String> transTypes = MainConst.TRANS_TYPE;
            put(CMD_INPUTACCOUNT_EMV, Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, edcTypes.get(0), edcTypes, edcTypes),
                    new NameValueSelectEntity(ManageItemValueSetter.TRANS_TYPE, transTypes.get(0), transTypes, transTypes),
                    new NameValueStringEntity(ManageItemValueSetter.AMOUNT, "100", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CASH_BACK_AMOUNT, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MAGNETIC_SWIPE_ENTRY_FLAG, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MANUAL_ENTRY_FLAG, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTACTLESS_ENTRY_FLAG, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTACT_EMV_ENTRY_FLAG, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.FALLBACK_SWIPE_ENTRY_FLAG, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.FALLBACK_INSERT_ENTRY_FLAG, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EXPIRY_DATE_PROMPT, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CVV_PROMPT, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ZIP_PROMPT, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_FLAG, "", InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, "", InputType.TYPE_CLASS_TEXT, "Inject key first"),
                    new NameValueStringEntity(ManageItemValueSetter.PADDING_CHAR, "", InputType.TYPE_CLASS_TEXT, "default is 0"),
                    new NameValueStringEntity(ManageItemValueSetter.TRACKDATA_SENTINEL, "1", InputType.TYPE_CLASS_TEXT, "1: need to include start and end sentinel. Default is 1."),
                    new NameValueStringEntity(ManageItemValueSetter.MIN_ACCOUNT_LENGTH, "10", InputType.TYPE_CLASS_TEXT, "default 10"),
                    new NameValueStringEntity(ManageItemValueSetter.MAX_ACCOUNT_LENGTH, "19", InputType.TYPE_CLASS_TEXT, "default 19"),
                    new NameValueStringEntity(ManageItemValueSetter.EMV_KERNEL_CONFIGURATION_SELECTION, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_DATE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_TIME, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_CODE, "840", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_EXPONENT, "02", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MERCHANT_CATEGORY_CODE, "", InputType.TYPE_CLASS_TEXT, "4 digits"),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_SEQUENCE_NUMBER, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAG_LIST, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_CVM_LIMIT, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KSN_FLAG, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(FullIntegrationValueSetter.REPORT_STATUS, "", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CUSTOM_DATA, "", InputType.TYPE_CLASS_TEXT, "Split strs by |, eg. str0|str1"),
                    new NameValueStringEntity(ManageItemValueSetter.FALLBACK_MANUAL_ENTRY_FLAG, "", InputType.TYPE_CLASS_TEXT, ""),
                    new ButtonEntity(ManageItemValueSetter.Custom_MAC_Information, "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", new NameValueStringEntity.ClickCallback() {
                        @Override
                        public void onClick(View v, final NameValueStringEntity entity) {
                            setActivityResultReceiver(new ActivityResultReceiver() {
                                @Override
                                public void onReceive(String data) {
                                    LogStaticWrapper.getLog().v("data:"+data);
                                    entity.setValue(formatSpecialData(data));
                                    customMacDataJson = data;
                                    UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                                }
                            });
                            Intent intent = new Intent(getContext(), CustomMACInformationActivity.class);
                            intent.putExtra("custom_mac_information", customMacDataJson);
                            startActivityForResult(intent, Constant.MANAGE_HMAC_INFORMATION);
                        }})
                    ));
            put(CMD_SET_EMV_TLV_DATA, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.EMV_DATA, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(FullIntegrationValueSetter.SET_TLV_TYPE, "", InputType.TYPE_CLASS_TEXT, "")
            ));

            put(CMD_GET_EMV_TLV_DATA, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.TLV_TYPE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAG_LIST, "", InputType.TYPE_CLASS_TEXT, "")
                    ));
            put(CMD_REMOVE_CARD, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.MESSAGE_1, "Msg1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MESSAGE_2, "Msg2", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PINPAD_TYPE, "0", InputType.TYPE_CLASS_NUMBER, "")
            ));
            put(CMD_COMPLETE_ONLINE_EMV, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.ONLINE_AUTH_RESULT, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.RESPONSE_CODE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.AUTHORIZATION_CODE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_AUTH_DATA, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_SCRIPT_1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ISSUER_SCRIPT_2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAG_LIST, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, "0", InputType.TYPE_CLASS_NUMBER, "")
            ));
            put(CMD_AUTHORIZECARD, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.AMOUNT, "100", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CASH_BACK_AMOUNT, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.MERCHANT_DECISION, "0", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_TYPE, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MIN_LENGTH, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MAX_LENGTH, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.NULL_PIN_FLAG, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_ALGORITHM, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EMV_KERNEL_CONFIGURATION_SELECTION, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_DATE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TRANSACTION_TIME, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_CODE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CURRENCY_EXPONENT, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.TAG_LIST, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.CONTINUOUS_SCREEN, "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PINPAD_TYPE, "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KSN_FLAG, "", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_MOCK_TRANSACTION, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(FullIntegrationValueSetter.MOCK_HOST, SharedPreferenceHelper.get(SharedPrefKey.CURRENT_MOCK_HOST, "gateway-sb.clearent.net"), InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(FullIntegrationValueSetter.MOCK_PORT, SharedPreferenceHelper.get(SharedPrefKey.CURRENT_MOCK_PORT, "443"), InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(FullIntegrationValueSetter.MOCK_PATH, SharedPreferenceHelper.get(SharedPrefKey.CURRENT_MOCK_PATH, "/rest/v2/transactions"), InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(FullIntegrationValueSetter.MOCK_API_KEY, "d9b2223d789c4325a4fb5435102f719b", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.AMOUNT, "1.00", InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(FullIntegrationValueSetter.MOCK_EXPIRY, "1222", InputType.TYPE_CLASS_NUMBER, "MMYY"),
                    new NameValueStringEntity(FullIntegrationValueSetter.MOCK_TRACK2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.EMV_DATA, "", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_GET_PIN_BLOCK, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.ACCOUNT_NUMBER, "5454545454545454", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.ENCRYPTION_TYPE, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KEY_SLOT, "1", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MIN_LENGTH, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_MAX_LENGTH, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.NULL_PIN_FLAG, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PIN_ALGORITHM, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameStringWithUnitEntity(ManageItemValueSetter.TIME_OUT, "300", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, edcTypes.get(0), edcTypes, edcTypes),
                    new NameValueSelectEntity(ManageItemValueSetter.TRANS_TYPE, transTypes.get(0), transTypes, transTypes),
                    new NameValueStringEntity(ManageItemValueSetter.TITLE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.PINPAD_TYPE, "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity(ManageItemValueSetter.KSN_FLAG, "", InputType.TYPE_CLASS_TEXT, "")
            ));

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
                    bundle2.putString(ManageFragment.INTENT_UPLOAD_IMAGE_PATH, imgFile);
                    intent2.putExtras(bundle2);
                    startActivityForResult(intent2, Constant.MANAGE_UPLOAD_IMAGE_RESULT);
                }
            };
            put(CMD_UPDATE_RESOURCE, Arrays.<RenderEntity>asList(
                    new NameValueBrowserEntity(FullIntegrationValueSetter.RESOURCE_PATH, "Browse", Environment.getExternalStorageDirectory().getAbsolutePath(), InputType.TYPE_CLASS_TEXT, "", browserClick) ,
                    new NameValueStringEntity(FullIntegrationValueSetter.FILE_TYPE, "0", InputType.TYPE_CLASS_NUMBER, "Default 0"),
                    new NameValueStringEntity(FullIntegrationValueSetter.TARGET_DEVICE, "0", InputType.TYPE_CLASS_NUMBER, "Default 0"),
                    new NameStringWithUnitEntity(FullIntegrationValueSetter.TIMEOUT, "1200", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_CHECK_FILE, Arrays.<RenderEntity>asList(
                    new NameValueStringEntity(ManageItemValueSetter.FILE_NAME, "", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_INIT, Arrays.<RenderEntity>asList());

            put(CMD_GETVAR, Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, edcTypes.get(0), edcTypes, edcTypes),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME3, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME4, "", InputType.TYPE_CLASS_TEXT, "")
            ));
            put(CMD_SETVAR, Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity(ManageItemValueSetter.EDC_TYPE, edcTypes.get(0), edcTypes, edcTypes),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE1, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE2, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME3, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE3, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_NAME4, "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity(ManageItemValueSetter.VAR_VALUE4, "", InputType.TYPE_CLASS_TEXT, "")
            ));
            final NameValueSelectEntity vasProgramEntity = new NameValueSelectEntity(ManageItemValueSetter.VAS_PROGRAM, "1",
                    Arrays.asList(MainApplication.getInstance().getResources().getStringArray(R.array.VAS_PROGRAMS)),
                    Arrays.asList("1", "2"));
            put(CMD_VAS_SET_MERCHANT_PARAMETERS, Arrays.<RenderEntity>asList(
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
            put(CMD_VAS_PUSH_DATA, Arrays.<RenderEntity>asList(
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
        }
    };

    private final Map<String, BaseRequest> requestMap = new HashMap<String, BaseRequest>() {
        {
            put(CMD_INPUTACCOUNT_EMV, new InputAccount.InputAccountRequest());
            put(CMD_SET_EMV_TLV_DATA, new EMVTLVData.SetTLVRequest());
            put(CMD_GET_EMV_TLV_DATA, new EMVTLVData.GetTLVRequest());
            put(CMD_REMOVE_CARD, new RemoveCard.RemoveCardRequest());
            put(CMD_COMPLETE_ONLINE_EMV, new CompleteOnlineEMV.CompleteOnlineEMVRequest());
            put(CMD_AUTHORIZECARD, new AuthorizeCard.AuthorizeRequest());
            put(CMD_MOCK_TRANSACTION, new MockRequest());
            put(CMD_GET_PIN_BLOCK, new GetPINBlock.GetPINBlockRequest());
            put(CMD_UPDATE_RESOURCE, new UpdateResource.UpdateResourceRequest());
            put(CMD_CHECK_FILE, new CheckFile.CheckFileRequest());
            put(CMD_GETVAR, new Variable.GetVariableRequest());
            put(CMD_SETVAR, new Variable.SetVariableRequest());
            put(CMD_VAS_SET_MERCHANT_PARAMETERS, new VasSetMerchantParameters.VasSetMerchantParametersRequest());
            put(CMD_VAS_PUSH_DATA, new VasPushData.VasPushDataRequest());
        }
    };

    private final Map<String, Map<String, FullIntegrationValueSetter>> valueSetterMap = new HashMap<String, Map<String, FullIntegrationValueSetter>>() {
        {
            put(CMD_INPUTACCOUNT_EMV, FullIntegrationValueSetter.INPUT_ACCOUNT_MAP);
            put(CMD_SET_EMV_TLV_DATA, FullIntegrationValueSetter.SET_TLV_MAP);
            put(CMD_GET_EMV_TLV_DATA, FullIntegrationValueSetter.GET_TLV_MAP);
            put(CMD_REMOVE_CARD, FullIntegrationValueSetter.REMOVE_CARD_MAP);
            put(CMD_COMPLETE_ONLINE_EMV, FullIntegrationValueSetter.COMPLETE_EMV_MAP);
            put(CMD_AUTHORIZECARD, FullIntegrationValueSetter.AUTHORIZE_MAP);
            put(CMD_MOCK_TRANSACTION, FullIntegrationValueSetter.MOCK_MAP);
            put(CMD_GET_PIN_BLOCK, FullIntegrationValueSetter.GET_PIN_BLOCK_MAP);
            put(CMD_UPDATE_RESOURCE, FullIntegrationValueSetter.UPDATE_RESOURCE_MAP);
            put(CMD_CHECK_FILE, FullIntegrationValueSetter.CHECK_FILE_MAP);
            put(CMD_GETVAR, FullIntegrationValueSetter.GET_VAR_MAP);
            put(CMD_SETVAR, FullIntegrationValueSetter.SET_VAR_MAP);
            put(CMD_VAS_SET_MERCHANT_PARAMETERS, FullIntegrationValueSetter.VAS_SET_PARAMETERS);
            put(CMD_VAS_PUSH_DATA, FullIntegrationValueSetter.VAS_PUSH_DATA);
        }
    };

    public FullIntegrateRequestFragment() {
        // Required empty public constructor
    }

    public static FullIntegrateRequestFragment newInstance() {
        return new FullIntegrateRequestFragment();
    }

    public static FullIntegrateRequestFragment newInstance(String processName) {
        FullIntegrateRequestFragment fragment = new FullIntegrateRequestFragment();
        fragment.setProcessBtn(processName);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_manage_request;
    }

    @Override
    protected void initView(View view) {
        requestContainer = view.findViewById(R.id.manage_request_container);

        cmdEdit = view.findViewById(R.id.manage_request_transType);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, CMDS);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmdEdit.setAdapter(arrayAdapter);
        cmdEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String command = CMDS.get(position);
                showCorrespondingRequestView(command);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button button = view.findViewById(R.id.setPED_btn);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnterPINDialog enterPINDialog = new EnterPINDialog(getActivity());
                enterPINDialog.show();
                enterPINDialog.showOKButton();
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
        for (Object obj : requestRenderEntityList) {
            if (obj instanceof RenderEntity) {
                RenderEntity renderEntity = (RenderEntity) obj;
                String name = ((NameValueEntity) renderEntity).getName();
                CommonItemView itemView = renderEntity.createView(requestContainer);
                requestContainer.addView(itemView.getView());
                itemView.getView().setTag(itemView);
                itemView.render(renderEntity);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.MANAGE_UPLOAD_IMAGE_RESULT:
                if (resultCode == RESULT_OK) {
                    onActivityResultReceive(data.getStringExtra(ManageFragment.INTENT_UPLOAD_IMAGE_PATH));
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
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onClickProcessBtn(View v) {
        preRequest(getRequest());
    }

    @Override
    public BaseRequest getRequest() {
        final String cmd = cmdEdit.getSelectedItem().toString();
        BaseRequest request = requestMap.get(cmd);
        setManageRequest(cmd, request);
        return request;
    }

    private String formatSpecialData(String commercialJson) {
        String result = "";
        try {
            JSONObject jsonObject = new JSONObject(commercialJson);
            Iterator it = jsonObject.keys();
            String key;
            while (it.hasNext()) {
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

    private void setManageRequest(String cmd, BaseRequest request) {
        for (Object renderEntity : requestRenderEntityList) {
            if (renderEntity instanceof NameValueSelectEntity) {
                NameValueSelectEntity entity = (NameValueSelectEntity) renderEntity;
                FullIntegrationValueSetter valueSetter = valueSetterMap.get(cmd).get(entity.getName());
                valueSetter.onSet(request, entity.getItemValues().get(entity.getSelectedItem()));
            } else if (renderEntity instanceof NameValueStringEntity) {
                NameValueStringEntity nameValueStringEntity = (NameValueStringEntity) renderEntity;
                FullIntegrationValueSetter valueSetter = valueSetterMap.get(cmd).get(nameValueStringEntity.getName());
                if (ManageItemValueSetter.VAS_SPECIAL_DATA.equals(nameValueStringEntity.getName())) {
                    valueSetter.onSet(request, specialDataJson);
                } else if (ManageItemValueSetter.VAS_SMART_TAP_DATA.equals(nameValueStringEntity.getName())) {
                    valueSetter.onSet(request, vasData);
                } else if(ManageItemValueSetter.Custom_MAC_Information.equals(nameValueStringEntity.getName())){
                    valueSetter.onSet(request, customMacDataJson);
                } else {
                    valueSetter.onSet(request, nameValueStringEntity.getValue());
                }
            }
        }
    }

    public String getRequestCmd() {
        return cmdEdit.getSelectedItem().toString();
    }

}

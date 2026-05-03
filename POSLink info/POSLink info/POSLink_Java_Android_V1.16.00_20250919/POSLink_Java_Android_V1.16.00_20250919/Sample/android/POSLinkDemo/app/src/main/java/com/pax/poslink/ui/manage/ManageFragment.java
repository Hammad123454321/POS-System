package com.pax.poslink.ui.manage;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pax.poslink.ManageRequest;
import com.pax.poslink.ManageResponse;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.ui.SigDetailActivity;
import com.pax.poslink.ui.base.RequestResponseFragment;
import com.pax.poslink.ui.pay.PaymentFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.CountRunTime;
import com.pax.poslink.util.POSLinkThreadPool;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.util.view.FloatViewUtil;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageFragment extends RequestResponseFragment<ManageRequest, ManageResponse, ManageRequestFragment>
        implements OnTouchListener, ProcessProgressDialog.OnSetListener {
    public static final String INTENT_UPLOAD_IMAGE_PATH = "UploadImagePath";

    private static final List<String> CANCELLABLE_BTN_LIST = Arrays.asList(
            "SHOWDIALOG", "DOSIGNATURE", "INPUTTEXT", "INPUTACCOUNT", "INPUTACCOUNTWITHEMV", "SHOWTEXTBOX", "SHOWDIALOGFORM", "MIFARECARD", "SHOWTHANKYOU", "SHOWMESSAGECENTER"
    );

    private static final List<String> NO_PROCESS_DIALOG_LIST = Arrays.asList("SHOWITEM");

    public static ManageFragment newInstance() {
        return new ManageFragment();
    }

    @Override
    protected ManageRequestFragment createRequestFragment() {
        return ManageRequestFragment.newInstance();
    }

    @Override
    protected ManageResponse getResponse() {
        return poslink.ManageResponse;
    }

    @Override
    public void onPreRequest(ManageRequest request) {
        if (responseLayout.getVisibility() == View.VISIBLE)
            initManageResponse();
        super.onPreRequest(request);
    }

    @Override
    public void run() {
        Log.i(TAG, "ManageRequest.TransType = " + request.TransType);

        // set the folder where to read the "comsetting.ini" file
        poslink.appDataFolder = getContext().getFilesDir().getAbsolutePath();
        poslink.SetCommSetting(SettingINI.getCommSettingFromFile(poslink.appDataFolder + "/" + SettingINI.FILENAME));

        poslink.ManageRequest = request;

        POSLinkThreadPool.getInstance().runUIThread(new Runnable() {
            @Override
            public void run() {
                FloatViewUtil.Companion.getInstance().showFloatView(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        poslink.CancelTrans();
                    }
                });
            }
        });

        //poslink.debug_mode = 1;

        // ProcessTrans is Blocking call, will return when the transaction is
        // complete.
        CountRunTime.start("Manage");
        ptr = poslink.ProcessTrans();

        CountRunTime.countPoint("Manage");

        // Recommend to use single thread pool instead.
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                ManageResponse response = (ManageResponse) msg.obj;
                setManageResponse(response);
                break;
            case Constant.TRANSACTION_TIMEOOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
        }
        FloatViewUtil.Companion.getInstance().hideFloatView();
    }

    private void setManageResponse(ManageResponse response) {
        if (responseLayout.getVisibility() == View.GONE) {
            responseLayout.setVisibility(View.VISIBLE);
        }
        responseRenderEntityList.clear();
        responseContainer.removeAllViews();

        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Result Code", response.ResultCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Result Text", response.ResultTxt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SN", response.SN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Block Value", response.BlockValue));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Model Name", response.ModelName));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("OS Version", response.OSVersion));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MacAddress", response.MacAddress));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Lines Per Screen", response.LinesPerScreen));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Chars Per Line", response.CharsPerLine));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Var Value", response.VarValue));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Var Value1", response.VarValue1));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Var Value2", response.VarValue2));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Var Value3", response.VarValue3));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Var Value4", response.VarValue4));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Button ID", response.ButtonNum));
        responseRenderEntityList.add(new NameValueBrowserEntity("Sig File Name", "Detail", response.SigFileName, InputType.TYPE_NULL, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, NameValueStringEntity entity) {
                if(TextUtils.isEmpty(entity.getValue())) {
                    Toast.makeText(getActivity(),"signature file path null" , Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), SigDetailActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("SigPath", entity.getValue());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Pin Block", response.PinBlock));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("KSN", response.KSN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Entry Mode", response.EntryMode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ETB", response.EncryptionTransmissionBlock));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Track1 Data", response.Track1Data));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Track2 Data", response.Track2Data));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Track3 Data", response.Track3Data));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PAN", response.PAN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Expiry Date", response.ExpiryDate));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ServiceCode", response.ServiceCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CVV", response.CVVCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Zip", response.ZipCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PinBypassStatus", response.PinBypassStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CardHolder", response.CardHolderName));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("QR Code", response.QRCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("BarcodeType", response.BarcodeType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("BarcodeData", response.BarcodeData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Text", response.Text));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ContactlessTransactionPath", response.ContactlessTransactionPath));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Authorization Result", response.AuthorizationResult));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Signature Flag", response.SignatureFlag));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MaskedPAN", response.MaskedPAN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Online PIN Flag", response.OnlinePINFlag));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TagList", response.TagList));   
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EMV TLV Data", response.EMVData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EncryptedEMVTLVData", response.EncryptedEMVTLVData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EncryptedSensitiveTLVData", response.EncryptedSensitiveTLVData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CardInsertStatus", response.CardInsertStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SignStatus", response.SignStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("LabelSelected", response.LabelSelected));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Token", response.Token));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TokenSN", response.TokenSN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("WifiMac", response.WifiMac));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Checksum", response.Checksum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Ext Data", response.ExtData));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SAFMode", response.SAFMode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("StartDateTime", response.StartDateTime));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EndDateTime", response.EndDateTime));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DurationInDays", response.DurationInDays));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MaxNumber", response.MaxNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TotalCeilingAmount", response.TotalCeilingAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CeilingAmountPerCardType", response.CeilingAmountPerCardType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("HALOPerCardType", response.HALOPerCardType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("UploadMode", response.UploadMode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AutoUploadIntervalTime", response.AutoUploadIntervalTime));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DeleteSAFConfirmation", response.DeleteSAFConfirmation));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity("VASCode", response.VASResponseInfo.VASCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("VASData", PaymentFragment.formatVASData(response.VASResponseInfo.VASData)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("NDEFData", response.VASResponseInfo.NDEFData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ResultData", response.ResultData));

//        responseRenderEntityList.add(new TitleItemEntity("Device Info"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MM ID", response.MultiMerchant.Id));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MM Name", response.MultiMerchant.Name));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("App Name", response.DeviceInfo.AppName));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("App Version", response.DeviceInfo.AppVersion));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CVM", response.Cvm));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TxnPath", response.TxnPath));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TxnResult", response.TxnResult));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("IssuerScriptResults", response.IssuerScriptResults));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DUKPTAvailableKeySlotCount", response.GetPEDInfoResponse.DUKPTAvailableKeySlotCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DUKPTKey", formatDUKPTKey(response.GetPEDInfoResponse.DUKPTKey)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MasterAvailableKeySlotCount", response.GetPEDInfoResponse.MasterAvailableKeySlotCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TMK", formatMasterKey(response.GetPEDInfoResponse.TMK)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SessionAvailableKeySlotCount", response.GetPEDInfoResponse.SessionAvailableKeySlotCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TPK", formatMasterKey(response.GetPEDInfoResponse.TPK)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TAK", formatMasterKey(response.GetPEDInfoResponse.TAK)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TDK", formatMasterKey(response.GetPEDInfoResponse.TDK)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AESDUKPTAvailableKeySlotCount", response.GetPEDInfoResponse.AESDUKPTAvailableKeySlotCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AESDUKPTKey", formatDUKPTKey(response.GetPEDInfoResponse.AESDUKPTKey)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Touchscreen", response.Touchscreen));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Pinpad Type", response.PinpadType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Luhn Validation Result", response.LuhnValidationResult));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Hardware Configuration Bitmap", response.HardwareConfigurationBitmap));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Custom Encrypted Data", getCustomEncryptedData(response)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("App Activated", response.AppActivated));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("License Expiry", response.LicenseExpiry));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Custom MAC Data", response.CustomMACInfo.Data));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Custom MAC KSN", response.CustomMACInfo.KSN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Encryption Result Data", response.EncryptionResultData));
        for (RenderEntity renderEntity : responseRenderEntityList) {
            if (renderEntity instanceof NameValueEntity && TextUtils.isEmpty(((NameValueEntity<String>) renderEntity).getValue())) {
                continue;
            }
            CommonItemView itemView = renderEntity.createView(responseContainer);
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
    }

    @NonNull
    private String getCustomEncryptedData(ManageResponse response) {
        return response.CustomEncryptedData.isEmpty() ? "" : new ArrayList<>(response.CustomEncryptedData).toString();
    }

    private String formatMasterKey(List<ManageResponse.MasterSessionKeyInfo> infos) {
        StringBuffer stringBuffer = new StringBuffer();
        for (ManageResponse.MasterSessionKeyInfo info : infos) {
            stringBuffer.append("KeySlot: " + info.KeySlot + "\n");
            stringBuffer.append("KCV: " + info.KCV + "\n");
        }
        return stringBuffer.toString();
    }

    private String formatDUKPTKey(List<ManageResponse.DUKPTKeyInfo> infos) {
        StringBuffer stringBuffer = new StringBuffer();
        for (ManageResponse.DUKPTKeyInfo info : infos) {
            stringBuffer.append("KeySlot: " + info.KeySlot + "\n");
            stringBuffer.append("KSN: " + info.KSN + "\n");
            stringBuffer.append("KCV: " + info.KCV + "\n");
        }
        return stringBuffer.toString();
    }

    private void initManageResponse() {
        setManageResponse(new ManageResponse());
    }

    /* 20120209 QC Bug No30 CombBox Start */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
    /* 20120209 QC Bug No30 CombBox End */

    @Override
    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
        if (cancelable) {
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel Process", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    poslink.CancelTrans();
                }
            });
        }
        if (enDismiss) {
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    poslink.CancelTrans();
                }
            });
        }
    }

    @Override
    public Dialog createDialog() {
        ManageRequestFragment fragment = findRequestFragment();
        if (NO_PROCESS_DIALOG_LIST.contains(fragment.getSelectedRequest())) {
            return null;
        }
        boolean needCancelBtn = CANCELLABLE_BTN_LIST.contains(fragment.getSelectedRequest());
        return ProcessProgressDialog.createDialog(getActivity(), getResources().getString(R.string.manage_process_prompt), needCancelBtn, false, this);
    }
}

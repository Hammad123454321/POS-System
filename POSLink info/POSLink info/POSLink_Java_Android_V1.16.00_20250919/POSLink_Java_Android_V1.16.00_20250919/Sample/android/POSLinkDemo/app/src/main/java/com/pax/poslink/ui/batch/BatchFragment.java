package com.pax.poslink.ui.batch;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.pax.poslink.BatchRequest;
import com.pax.poslink.BatchResponse;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.ui.base.RequestFragment;
import com.pax.poslink.ui.base.RequestResponseFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.view.TitleItemEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

public class BatchFragment extends RequestResponseFragment<BatchRequest, BatchResponse, BatchRequestFragment> implements OnTouchListener, ProcessProgressDialog.OnSetListener {

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                BatchResponse response = (BatchResponse)msg.obj;
                setBatchResponse(response);
                break;
            case Constant.TRANSACTION_TIMEOOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
        }
    }

    public static BatchFragment newInstance() {
        return new BatchFragment();
    }

    @Override
    protected BatchRequestFragment createRequestFragment() {
        return BatchRequestFragment.newInstance();
    }

    @Override
    protected BatchResponse getResponse() {
        return poslink.BatchResponse;
    }

    @Override
    public void onPreRequest(BatchRequest request) {
        if (responseLayout.getVisibility() == View.VISIBLE)
            initBatchResponse();
        super.onPreRequest(request);
    }

    @Override
    public void run() {
        //processTransactions
        Log.i(TAG, "BatchRequest.TransType = " + request.TransType);

        // set the folder whereto read the "comsetting.ini" file
        poslink.appDataFolder = getActivity().getApplicationContext().getFilesDir().getAbsolutePath();
        poslink.SetCommSetting(SettingINI.getCommSettingFromFile(poslink.appDataFolder + "/" + SettingINI.FILENAME));

        poslink.BatchRequest = request;
        // ProcessTrans is Blocking call, will return when the transaction is
        // complete.
        ptr = poslink.ProcessTrans();
    }

    private void setBatchResponse(BatchResponse response) {
        if (responseLayout.getVisibility() == View.GONE) {
            responseLayout.setVisibility(View.VISIBLE);
        }
        responseRenderEntityList.clear();
        responseContainer.removeAllViews();

        Resources resources = getResources();
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_resultCode), response.ResultCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_resultTxt), response.ResultTxt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_creditCount), response.CreditCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_creditAmount), response.CreditAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_debitCount), response.DebitCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_debitAmount), response.DebitAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_ebtCount), response.EBTCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_ebtAmount), response.EBTAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_giftCount), response.GiftCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_giftAmount), response.GiftAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_loyaltyCount), response.LoyaltyCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_loyaltyAmount), response.LoyaltyAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_cashCount), response.CashCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_cashAmount), response.CashAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_checkCount), response.CHECKCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_checkAmount), response.CHECKAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeCount), response.QRCodeCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeAmount), response.QRCodeAmount));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CreditTipCount), response.CreditTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CreditTipAmount), response.CreditTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_DebitTipCount), response.DebitTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_DebitTipAmount), response.DebitTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_GiftTipCount), response.GiftTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_GiftTipAmount), response.GiftTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_LoyaltyTipCount), response.LoyaltyTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_LoyaltyTipAmount), response.LoyaltyTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CashTipCount), response.CashTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CashTipAmount), response.CashTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeTipCount), response.QRCodeTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeTipAmount), response.QRCodeTipAmount));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_timestamp), response.Timestamp));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_tid), response.TID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_mid), response.MID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostTraceNum), response.HostTraceNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_batchNum), response.BatchNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_authCode), response.AuthCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostCode), response.HostCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostResponse), response.HostResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_message), response.Message));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_extData), response.ExtData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_SAFTotalCnt), response.SAFTotalCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_SAFTotalAmt), response.SAFTotalAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_UploadRecords), response.SAFUploadedCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_UploadAmt), response.SAFUploadedAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_FailedRecords), response.SAFFailedCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_TotalFailed), response.SAFFailedTotal));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_DeleteRecords), response.SAFDeletedCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_BatchFailedRefNum), response.BatchFailedRefNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_BatchFailedCount), response.BatchFailedCount));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_record_type), response.TORResponseInfo.RecordType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_reversal_time_stamp), response.TORResponseInfo.ReversalTimeStamp));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_host_response_code), response.TORResponseInfo.HostResponseCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_host_response_message), response.TORResponseInfo.HostResponseMessage));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_host_reference_number), response.TORResponseInfo.HostReferenceNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_gateway_transaction_id), response.TORResponseInfo.GatewayTransactionID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_orig_amount), response.TORResponseInfo.OrigAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_masked_pan), response.TORResponseInfo.MaskedPAN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_batch_no), response.TORResponseInfo.BatchNo));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_reversal_auth_code), response.TORResponseInfo.ReversalAuthCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_orig_trans_type), response.TORResponseInfo.OrigTransType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_orig_trans_date_time), response.TORResponseInfo.OrigTransDateTime));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.tor_info_response_orig_trans_auth_code), response.TORResponseInfo.OrigTransAuthCode));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MM ID", response.MultiMerchant.Id));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MM Name", response.MultiMerchant.Name));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledAmount), response.HostSettledAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledCount), response.HostSettledCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledReturnAmount), response.HostSettledReturnAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledReturnCount), response.HostSettledReturnCount));
        responseRenderEntityList.add(new TitleItemEntity(resources.getString(R.string.batch_response_totalTax)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_taxType), response.TotalTax.TaxType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_creditTotalTaxAmount), response.TotalTax.CreditTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_debitTotalTaxAmount), response.TotalTax.DebitTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_EBTTotalTaxAmount), response.TotalTax.EBTTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_giftTotalTaxAmount), response.TotalTax.GiftTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_loyaltyTotalTaxAmount), response.TotalTax.LoyaltyTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_checkTotalTaxAmount), response.TotalTax.CheckTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeTotalTaxAmount), response.TotalTax.QRCodeTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_cashTotalTax), response.TotalTax.CashTotalTax));
        responseRenderEntityList.add(new TitleItemEntity(resources.getString(R.string.batch_response_MerchantTotals)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_mid), response.MerchantTotals.MID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_tid), response.MerchantTotals.TID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_creditCount), response.MerchantTotals.CreditCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_creditAmount), response.MerchantTotals.CreditAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_debitCount), response.MerchantTotals.DebitCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_debitAmount), response.MerchantTotals.DebitAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_ebtCount), response.MerchantTotals.EBTCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_ebtAmount), response.MerchantTotals.EBTAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_giftCount), response.MerchantTotals.GiftCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_giftAmount), response.MerchantTotals.GiftAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_loyaltyCount), response.MerchantTotals.LoyaltyCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_loyaltyAmount), response.MerchantTotals.LoyaltyAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_cashCount), response.MerchantTotals.CashCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_cashAmount), response.MerchantTotals.CashAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_checkCount), response.MerchantTotals.CHECKCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_checkAmount), response.MerchantTotals.CHECKAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeCount), response.MerchantTotals.QRCodeCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeAmount), response.MerchantTotals.QRCodeAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CreditTipCount), response.MerchantTotals.CreditTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CreditTipAmount), response.MerchantTotals.CreditTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_DebitTipCount), response.MerchantTotals.DebitTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_DebitTipAmount), response.MerchantTotals.DebitTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_GiftTipCount), response.MerchantTotals.GiftTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_GiftTipAmount), response.MerchantTotals.GiftTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_LoyaltyTipCount), response.MerchantTotals.LoyaltyTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_LoyaltyTipAmount), response.MerchantTotals.LoyaltyTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CashTipCount), response.MerchantTotals.CashTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_CashTipAmount), response.MerchantTotals.CashTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeTipCount), response.MerchantTotals.QRCodeTipCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeTipAmount), response.MerchantTotals.QRCodeTipAmount));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_taxType), response.MerchantTotals.TotalTax.TaxType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_creditTotalTaxAmount), response.MerchantTotals.TotalTax.CreditTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_debitTotalTaxAmount), response.MerchantTotals.TotalTax.DebitTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_EBTTotalTaxAmount), response.MerchantTotals.TotalTax.EBTTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_giftTotalTaxAmount), response.MerchantTotals.TotalTax.GiftTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_loyaltyTotalTaxAmount), response.MerchantTotals.TotalTax.LoyaltyTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_checkTotalTaxAmount), response.MerchantTotals.TotalTax.CheckTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_QRCodeTotalTaxAmount), response.MerchantTotals.TotalTax.QRCodeTotalTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_cashTotalTax), response.MerchantTotals.TotalTax.CashTotalTax));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledAmount), response.MerchantTotals.HostSettledAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledCount), response.MerchantTotals.HostSettledCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledReturnAmount), response.MerchantTotals.HostSettledReturnAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostSettledReturnCount), response.MerchantTotals.HostSettledReturnCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostResponseCode), response.MerchantTotals.HostResponseCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.batch_response_hostResponseMessage), response.MerchantTotals.HostResponseMessage));


        for (RenderEntity renderEntity : responseRenderEntityList) {
            CommonItemView itemView = renderEntity.createView(responseContainer);
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
    }

    private void initBatchResponse() {
        setBatchResponse(new BatchResponse());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    @Override
    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
        //don't need to set a listener
    }

    @Override
    public Dialog createDialog() {
        return ProcessProgressDialog.createDialog(getActivity(), getResources().getString(R.string.batch_process_prompt),false, false, this);
    }
}

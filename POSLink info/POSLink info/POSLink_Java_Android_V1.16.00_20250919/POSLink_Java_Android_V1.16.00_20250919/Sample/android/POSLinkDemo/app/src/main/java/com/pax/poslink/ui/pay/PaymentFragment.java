package com.pax.poslink.ui.pay;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import com.pax.poslink.AbstractPosLink;
import com.pax.poslink.POSLinkCommon;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PaymentResponse;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.ui.base.RequestResponseFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.ConvertSigUtils;
import com.pax.poslink.util.LogStaticWrapper;
import com.pax.poslink.util.POSLinkThreadPool;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.util.view.FloatViewUtil;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.view.SingleButtonEntity;
import com.pax.poslink.view.TitleItemEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Created by Leon.F on 2018/1/19.
 */

public class PaymentFragment extends RequestResponseFragment<PaymentRequest, PaymentResponse, PayRequestFragment> implements OnTouchListener, ProcessProgressDialog.OnSetListener{
    private String mLastRequestTender = null;
    private String mLastRequestTrans = null;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                PaymentResponse response = (PaymentResponse) msg.obj;
                setPaymentResponse(response);
                if (response.SignData.length() != 0 && response.SigFileName.length() != 0) {
                    PayRequestFragment fragment = findRequestFragment();
                    if (TextUtils.isEmpty(fragment.getSigBrowserValue())) {
                        poslink.appDataFolder = getActivity().getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
                        String folder = poslink.appDataFolder + "/img/receipt";
                        File fileDis = new File(folder);
                        fileDis.mkdirs();
                        fragment.updateSigBrowserIfNeeded(folder);
                    }
                    String tmp = fragment.getSigBrowserValue();
                    String sigfilepath = tmp + "/" + response.SigFileName + ".png";
                    try {
                        int ret = ConvertSigUtils.convertSigToPic(response.SignData, "png", sigfilepath);
                        Log.e(TAG, "SignData" + response.SignData);
                        Log.e(TAG, "Signfile:" + sigfilepath);
                        Log.e(TAG, "ret" + ret);
//                        if (ret < 0)
//                            UIUtil.showToast(getActivity(), "save image fail", Toast.LENGTH_LONG);
////                            Toast.makeText(getActivity(), "save image fail", Toast.LENGTH_LONG).show();
//                        else
//                            UIUtil.showToast(getActivity(), "save image sucess", Toast.LENGTH_LONG);
////                            Toast.makeText(getActivity(), "save image sucess", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Constant.TRANSACTION_TIMEOOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
            case Constant.TRANSACTION_STATUS:
                String sTitle = "REPORTED STATUS";
                String sMessage = msg.obj.toString();
                DialogUtils.showMsgDialog(getActivity(), sTitle, sMessage);
                break;
        }
        FloatViewUtil.Companion.getInstance().hideFloatView();
    }

    public static PaymentFragment newInstance() {
        return new PaymentFragment();
    }

    @Override
    protected PayRequestFragment createRequestFragment() {
        return PayRequestFragment.newInstance();
    }

    @Override
    protected PaymentResponse getResponse() {
        return poslink.PaymentResponse;
    }

    @Override
    public void onPreRequest(PaymentRequest request) {
        if (responseLayout.getVisibility() == View.VISIBLE)
            initPaymentResponse();
        super.onPreRequest(request);
    }

    private int lastReportedStatus = -1;
    private AbstractPosLink.ReportStatusListener reportStatusListener = new AbstractPosLink.ReportStatusListener() {
        @Override
        public void onReportStatus(int status) {
            Log.d("justin", Log.getStackTraceString(new Throwable()));
            if (status != lastReportedStatus) {
                switch (status) {
                    case 0:
                        Message msg0 = new Message();
                        msg0.what = Constant.TRANSACTION_STATUS;
                        msg0.obj = "Ready for CARD INPUT";
                        mHandler.sendMessage(msg0);
                        break;
                    case 1:
                        Message msg1 = new Message();
                        msg1.what = Constant.TRANSACTION_STATUS;
                        msg1.obj = "Ready for PIN ENTRY";
                        mHandler.sendMessage(msg1);
                        break;
                    case 2:
                        Message msg2 = new Message();
                        msg2.what = Constant.TRANSACTION_STATUS;
                        msg2.obj = "Ready for SIGNATURE";
                        mHandler.sendMessage(msg2);
                        break;
                    case 3:
                        Message msg3 = new Message();
                        msg3.what = Constant.TRANSACTION_STATUS;
                        msg3.obj = "Ready for ONLINE PROCESSING";
                        mHandler.sendMessage(msg3);
                        break;
                    case 4:
                        Message msg4 = new Message();
                        msg4.what = Constant.TRANSACTION_STATUS;
                        msg4.obj = "Ready for NEW CARD INPUT";
                        mHandler.sendMessage(msg4);
                        break;
                    case 5:
                        Message msg5 = new Message();
                        msg5.what = Constant.TRANSACTION_STATUS;
                        msg5.obj = "Ready for Signature retry by pressing CLEAR key";
                        mHandler.sendMessage(msg5);
                        break;
                    case 6:
                        Message msg6 = new Message();
                        msg6.what = Constant.TRANSACTION_STATUS;
                        msg6.obj = " Ready for PIN retry by inputting wrong offline PIN for EMV.";
                        mHandler.sendMessage(msg6);
                        break;
                    case 9020002:
                        Message msg7 = new Message();
                        msg7.what = Constant.TRANSACTION_STATUS;
                        msg7.obj = "Ready for Entering Cashback.";
                        mHandler.sendMessage(msg7);
                        break;
                    default:
                        //do nothing
                        break;
                }

                lastReportedStatus = status;
            }
        }
    };
//    final Runnable MyRunnable = new Runnable() {
//
//        public void run() {
//            int status;
//
//            try {
//                Thread.sleep(0);
//
//                status = poslink.GetReportedStatus();
//                if (status != lastReportedStatus) {
//                    switch (status) {
//                        case 0:
//                            Message msg0 = new Message();
//                            msg0.what = Constant.TRANSACTION_STATUS;
//                            msg0.obj = "Ready for CARD INPUT";
//                            mHandler.sendMessage(msg0);
//                            break;
//                        case 1:
//                            Message msg1 = new Message();
//                            msg1.what = Constant.TRANSACTION_STATUS;
//                            msg1.obj = "Ready for PIN ENTRY";
//                            mHandler.sendMessage(msg1);
//                            break;
//                        case 2:
//                            Message msg2 = new Message();
//                            msg2.what = Constant.TRANSACTION_STATUS;
//                            msg2.obj = "Ready for SIGNATURE";
//                            mHandler.sendMessage(msg2);
//                            break;
//                        case 3:
//                            Message msg3 = new Message();
//                            msg3.what = Constant.TRANSACTION_STATUS;
//                            msg3.obj = "Ready for ONLINE PROCESSING";
//                            mHandler.sendMessage(msg3);
//                            break;
//                        case 4:
//                            Message msg4 = new Message();
//                            msg4.what = Constant.TRANSACTION_STATUS;
//                            msg4.obj = "Ready for NEW CARD INPUT";
//                            mHandler.sendMessage(msg4);
//                            break;
//                        case 5:
//                            Message msg5 = new Message();
//                            msg5.what = Constant.TRANSACTION_STATUS;
//                            msg5.obj = "Ready for Signature retry by pressing CLEAR key";
//                            mHandler.sendMessage(msg5);
//                            break;
//                        case 6:
//                            Message msg6 = new Message();
//                            msg6.what = Constant.TRANSACTION_STATUS;
//                            msg6.obj = " Ready for PIN retry by inputting wrong offline PIN for EMV.";
//                            mHandler.sendMessage(msg6);
//                            break;
//                        case 9020002:
//                            Message msg7 = new Message();
//                            msg7.what = Constant.TRANSACTION_STATUS;
//                            msg7.obj = "Ready for Entering Cashback.";
//                            mHandler.sendMessage(msg7);
//                            break;
//                        default:
//                            //do nothing
//                            break;
//                    }
//
//                    lastReportedStatus = status;
//                }
//
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            mHandler.postDelayed(this, 500);
//        }
//    };

    @Override
    public void run() {
//        Log.i(TAG, "payrequest.TenderType" + request.TenderType + ", payrequest.TransType"
//                + request.TransType);
        if (request == null)
            return;
        cacheLastTenderAndTransType();

        // set the folder to save the "comsetting.ini" file
        poslink.appDataFolder = getContext().getFilesDir().getAbsolutePath();
        poslink.SetCommSetting(SettingINI.getCommSettingFromFile(poslink.appDataFolder + "/" + SettingINI.FILENAME));

        poslink.PaymentRequest = request;
        poslink.setReportStatusListener(reportStatusListener);

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

//        mHandler.postDelayed(MyRunnable, 500);

        try {
            Thread.sleep(500);
            // ProcessTrans is Blocking call, will return when the transaction is
            // complete.
            ptr = poslink.ProcessTrans();
            poslink.setReportStatusListener(null);
            lastReportedStatus = -1;

//            mHandler.removeCallbacks(MyRunnable);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void cacheLastTenderAndTransType() {
        PayRequestFragment fragment = findRequestFragment();
        mLastRequestTender = fragment.getSelectedTenderType();
        mLastRequestTrans = fragment.getSelectedTransType();
    }

    /**
     * set response value in textview.
     *
     * @param response ""
     */
    private void setPaymentResponse(final PaymentResponse response) {
        if (responseLayout.getVisibility() == View.GONE) {
            responseLayout.setVisibility(View.VISIBLE);
        }

        responseRenderEntityList.clear();
        responseContainer.removeAllViews();

        Resources resources = getResources();
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_resultCode), response.ResultCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_resultTxt), response.ResultTxt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_authCode), response.AuthCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedAmt), response.ApprovedAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedTipAmt), response.ApprovedTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedCashBackAmount), response.ApprovedCashBackAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedMerchantFee), response.ApprovedMerchantFee));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedTaxAmount), response.ApprovedTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_avsResponse), response.AvsResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_bogusAccountNum), response.BogusAccountNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_cardType), response.CardType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_cvResponse), response.CvResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_hostCode), response.HostCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_hostResponse), response.HostResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_message), response.Message));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_refNum), response.RefNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_remainingBalance), response.RemainingBalance));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_extraBalance), response.ExtraBalance));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_requestedAmt), response.RequestedAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_timestamp), response.Timestamp));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_sigfilename), response.SigFileName));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_sigdata), response.SignData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_vasCode), String.valueOf(response.VASResponseInfo.VASCode)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_vasData), formatVASData(response.VASResponseInfo.VASData)));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_NDEFData), response.VASResponseInfo.NDEFData));
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
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_tic), response.TransactionIntegrityClass));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_gift_card_type), response.GiftCardType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_debit_account_type), response.DebitAccountType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_host_account), response.HostAccount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_host_card_type), response.HostCardType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_transaction_remaining_amount), response.TransactionRemainingAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_gateway_id), response.GatewayTransactionID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_host_detail_message), response.HostDetailedMessage));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_retrieval_reference_number), response.RetrievalReferenceNumber));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_edc_type), response.EDCType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_masked_pan), response.MaskedPAN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_track1_data), response.Track1Data));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_track2_data), response.Track2Data));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_track3_data), response.Track3Data));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_ps2000), response.PaymentService2000));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_authorization_response), response.AuthorizationResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_issuer_response_code), response.IssuerResponseCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_ecr_transaction_id), response.ECRTransID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_host_time_stamp), response.HostTimeStamp));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_accountReferenceID), response.PaymentAccountReferenceID));



        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_aci), response.AddlRspData.ACI));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_trans_id), response.AddlRspData.TransID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_card_level_result), response.AddlRspData.CardLevelResult));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_source_reason_code), response.AddlRspData.SourceReasonCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_bank_net_data), response.AddlRspData.BankNetData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_pos_entry_mode_chg), response.AddlRspData.POSEntryModeChg));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_tran_edit_errcode), response.AddlRspData.TranEditErrCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_disc_proc_code), response.AddlRspData.DiscProcCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_disc_pos_entry), response.AddlRspData.DiscPOSEntry));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_disc_resp_code), response.AddlRspData.DiscRespCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_pos_data), response.AddlRspData.POSData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_disc_trans_qualifier), response.AddlRspData.DiscTransQualifier));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_disc_nrid), response.AddlRspData.DiscNRID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_trnmsn_date_time), response.AddlRspData.TrnmsnDateTime));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_orig_stan), response.AddlRspData.OrigSTAN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_cvv_error_code), response.AddlRspData.CVVErrorCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_xcode_resp), response.AddlRspData.XCodeResp));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_ath_ntwk_id), response.AddlRspData.AthNtwkID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_term_entry_capablt), response.AddlRspData.TermEntryCapablt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_pos_entry_mode), response.AddlRspData.POSEntryMode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_serv_code), response.AddlRspData.ServCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_spend_qind), response.AddlRspData.SpendQInd));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_wlt_id), response.AddlRspData.WltID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_local_date_time), response.AddlRspData.LocalDateTime));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.response_addlrspdata_emv_tags), response.AddlRspData.EMVTags));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_extData), response.ExtData + getPayload(response)));

        responseRenderEntityList.add(new TitleItemEntity("Restaurant"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_tableNum), response.Restaurant.TableNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_guestNum), response.Restaurant.GuestNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_ticketNum), response.Restaurant.TicketNumber));

        responseRenderEntityList.add(new TitleItemEntity("Multi-Merchant"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_MMID), response.MultiMerchant.Id));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_MMName), response.MultiMerchant.Name));

        responseRenderEntityList.add(new TitleItemEntity("CardInfo"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_cardbin), response.CardInfo.CardBin));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_new_cardbin), response.CardInfo.NewCardBin));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_program_type), response.CardInfo.ProgramType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_host_program_type), response.CardInfo.HostProgramType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_dcc_capable), response.CardInfo.DccCapable));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_debit_capable), response.CardInfo.DebitCapable));

        responseRenderEntityList.add(new TitleItemEntity("Transaction information"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DiscountAmount", response.PaymentTransInfo.DiscountAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ChargedAmount", response.PaymentTransInfo.ChargedAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SignatureStatus", response.PaymentTransInfo.SignatureStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Fps", response.PaymentTransInfo.Fps));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("FpsSignature", response.PaymentTransInfo.FpsSignature));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("FpsReceipt", response.PaymentTransInfo.FpsReceipt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("OrigTip", response.PaymentTransInfo.OrigTip));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("edcType", response.PaymentTransInfo.EdcType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("token", response.PaymentTransInfo.Token));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("HRef", response.PaymentTransInfo.HRef));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SN", response.PaymentTransInfo.SN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PrintLine1", response.PaymentTransInfo.PrintLine1));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PrintLine2", response.PaymentTransInfo.PrintLine2));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PrintLine3", response.PaymentTransInfo.PrintLine3));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PrintLine4", response.PaymentTransInfo.PrintLine4));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PrintLine5", response.PaymentTransInfo.PrintLine5));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SettlementDate", response.PaymentTransInfo.SettlementDate));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("HostEchoData", response.PaymentTransInfo.HostEchoData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PinStatusNum", response.PaymentTransInfo.PinStatusNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EwicBenefitExpd", response.PaymentTransInfo.EwicBenefitExpd));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EwicBalance", response.PaymentTransInfo.EwicBalance));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EwicDetail", response.PaymentTransInfo.EwicDetail));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ValidationCode", response.PaymentTransInfo.ValidationCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("UserLanguageStatus", response.PaymentTransInfo.UserLanguageStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ReverseAmount", response.PaymentTransInfo.ReverseAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ReversalStatus", response.PaymentTransInfo.ReversalStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TokenSerialNum", response.PaymentTransInfo.TokenSerialNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("GlobalUid", response.PaymentTransInfo.GlobalUid));

        responseRenderEntityList.add(new TitleItemEntity("EMV Tag"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TC", response.PaymentEmvTag.Tc));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TVR", response.PaymentEmvTag.Tvr));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AID", response.PaymentEmvTag.Aid));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TSI", response.PaymentEmvTag.Tsi));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ATC", response.PaymentEmvTag.Atc));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("APPLAB", response.PaymentEmvTag.AppLabel));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("APPPN", response.PaymentEmvTag.AppPreferName));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("IAD", response.PaymentEmvTag.Iad));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ARC", response.PaymentEmvTag.Arc));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CID", response.PaymentEmvTag.Cid));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CVM", response.PaymentEmvTag.Cvm));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AC", response.PaymentEmvTag.Ac));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AIP", response.PaymentEmvTag.Aip));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AVN", response.PaymentEmvTag.Avn));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("IssuerAuthData", response.PaymentEmvTag.IssuerAuthData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CDOL2", response.PaymentEmvTag.Cdol2));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("HRED", response.PaymentEmvTag.Hred));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TacDefault", response.PaymentEmvTag.TacDefault));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TacDenial", response.PaymentEmvTag.TacDenial));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TacOnline", response.PaymentEmvTag.TacOnline));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("IacDefault", response.PaymentEmvTag.IacDefault));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("IacDenial", response.PaymentEmvTag.IacDenial));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("IacOnline", response.PaymentEmvTag.IacOnline));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AUC", response.PaymentEmvTag.Auc));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ARQC", response.PaymentEmvTag.Arqc));

        responseRenderEntityList.add(new TitleItemEntity("Fleet Card"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Odometer", response.FleetCard.Odometer));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("VehicleNumber", response.FleetCard.VehicleNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("JobNumber", response.FleetCard.JobNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DriverId", response.FleetCard.DriverId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("EmployeeNumber", response.FleetCard.EmployeeNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("LicenseNumber", response.FleetCard.LicenseNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("JobId", response.FleetCard.JobId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DepartmentNumber", response.FleetCard.DepartmentNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CustomerData", response.FleetCard.CustomerData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("UserId", response.FleetCard.UserId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("VehicleId", response.FleetCard.VehicleId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PurchaseRestrictions", response.FleetCard.PurchaseRestrictions));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("RestrictedProductData", response.FleetCard.RestrictedProductData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Hubometer", response.FleetCard.Hubometer));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MainTenanceId", response.FleetCard.MainTenanceId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("FleetPoNumber", response.FleetCard.FleetPoNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ReeferHours", response.FleetCard.ReeferHours));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TrailerId", response.FleetCard.TrailerId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TripNumber", response.FleetCard.TripNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("UnitId", response.FleetCard.UnitId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AdditionalFleetData1", response.FleetCard.AdditionalFleetData1));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AdditionalFleetData2", response.FleetCard.AdditionalFleetData2));

        responseRenderEntityList.add(new SingleButtonEntity("SHOW RECEIPT", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(View v, SingleButtonEntity renderEntity) {
                PaymentResponse response = getResponse();
                if (response == null || (!response.ResultCode.contains("000000") && !response.ResultCode.contains("000100"))) {
                    Toast.makeText(getContext(), "No Receipt!", Toast.LENGTH_LONG).show();
                } else if (response.ResultCode.contains("000100")) {
                    Toast.makeText(getContext(), "Don't Print Receipt For Decline", Toast.LENGTH_LONG).show();
                } else {
                    String receipt = generateReceipt();
                    String receiptData = generateReceiptData();
                    Intent intent = new Intent(getActivity(), PaymentReceiptActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("Payment_Receipt", receipt);
                    bundle.putString("Payment_Receipt_Data", receiptData);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        }));
        responseRenderEntityList.add(new TitleItemEntity("Host Credential Information"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Host TID", response.HostCredentialInfo.HostTID));


        CommonItemView tempEntity = null;
        Map<CommonItemView, Boolean> titleViewMap = new HashMap<>();
        for (RenderEntity renderEntity : responseRenderEntityList) {
            if (renderEntity instanceof NameValueEntity && TextUtils.isEmpty(((NameValueEntity<String>) renderEntity).getValue())) {
                continue;
            }
            CommonItemView itemView = renderEntity.createView(responseContainer);
            if (renderEntity instanceof TitleItemEntity) {
                tempEntity = itemView;
                titleViewMap.put(tempEntity, false);
            }
            if (renderEntity instanceof NameValueEntity) {
                if (tempEntity != null) {
                    titleViewMap.put(tempEntity, true);
                }
            }
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
        for (Map.Entry<CommonItemView, Boolean> entry : titleViewMap.entrySet()) {
            if (!entry.getValue()) {
                responseContainer.removeView(entry.getKey().getView());
            }
        }
    }

    private String getPayload(PaymentResponse response) {
        if (StringUtil.isEmpty(response.PayloadData)){
            return "";
        }
        Document document = null;
        TransformerFactory tf = TransformerFactory.newInstance();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
            Element stmp = document.createElement("payload");
            stmp.appendChild(document.createTextNode(response.PayloadData));
            document.appendChild(stmp);
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                    "yes");
            ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            PrintWriter pw = new PrintWriter(bout);
            StreamResult result = new StreamResult(pw);

            transformer.transform(source, result);
            String payload = bout.toString(POSLinkCommon.BYTE_TO_STRING_CHARSET);
            return payload;
        } catch (ParserConfigurationException e) {
            com.pax.poslink.Log.exceptionLog(e);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * clear response value of textview.
     */
    private void initPaymentResponse() {
        setPaymentResponse(new PaymentResponse());
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
            LogStaticWrapper.getLog().d("ProcessProgressDialog, onSetListener cancelable");
            dialog.setButton("Cancel Process", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LogStaticWrapper.getLog().d("ProcessProgressDialog, click cancel");
                    poslink.CancelTrans();
                }
            });
        }
        if (enDismiss) {
            LogStaticWrapper.getLog().d("ProcessProgressDialog, onSetListener dismiss");
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    LogStaticWrapper.getLog().d("ProcessProgressDialog, onDismiss");
                    poslink.CancelTrans();
                }
            });
        }
    }

    @Override
    public Dialog createDialog() {
        return ProcessProgressDialog.createDialog(getActivity(), getResources().getString(R.string.payment_process_prompt), true, false, this);
    }

    private String generateReceiptData() {
        PaymentResponse response = poslink.PaymentResponse;
        POSLinkPrinter.PrintDataFormatter printDataFormatter = new POSLinkPrinter.PrintDataFormatter();
        printDataFormatter.addHeader()
                .addLineSeparator()
                .addLeftAlign().addDate().addRightAlign().addTime()
                .addLineSeparator()
                .addLineSeparator()
                .addLeftAlign().addContent(mLastRequestTender + " " + mLastRequestTrans + ":")
                .addLineSeparator()
                .addLineSeparator()
                .addLeftAlign().addContent("Transaction #:").addRightAlign().addContent(response.RefNum)
                .addLineSeparator()
                .addLeftAlign().addContent("Card Type:").addRightAlign().addContent(response.CardType)
                .addLineSeparator()
                .addLeftAlign().addContent("Account:").addRightAlign().addContent(response.BogusAccountNum)
                .addLineSeparator();
        String left = "Entry";
        String temp = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "PLEntryMode");
        String right = "";
        if (temp.contains("0")) {
            right = "Manual";
        } else if (temp.contains("1")) {
            right = "Swipe";
        } else if (temp.contains("2")) {
            right = "Contactless";
        } else if (temp.contains("3")) {
            right = "Scanner";
        } else if (temp.contains("4")) {
            right = "Chip";
        } else if (temp.contains("5")) {
            right = "Chip Fall Back Swipe";
        }

        if (temp.length() > 0){
            printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                    .addLineSeparator();
        }
        if (response.ResultCode.contains("000000")) {
            //amount
            temp = response.ApprovedAmount;
            if (temp.length() > 0) {
                left = "Amount:";

                int len = temp.length();
                if (len == 2) {
                    right = "$" + "0." + temp;
                } else if (len == 1) {
                    right = "$" + "0.0" + temp;
                } else {
                    right = "$" + temp.substring(0, len - 2) + "." + temp.substring(len - 2);
                }
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //order number
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "MOTOECommerceOrderNum");
            if (right.length() > 0) {
                left = "Order Number";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            printDataFormatter.addLineSeparator();

            //ref
            right = response.HostCode;
            if (right.length() > 0) {
                left = "Ref. Number:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //VALCODE
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "ValCode");
            if (right.length() > 0) {
                left = "ValCode:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //auth code
            right = response.AuthCode;
            if (right.length() > 0) {
                left = "Auth Code:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //response
            right = response.Message;
            if (right.length() > 0) {
                left = "Response:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //TC
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "TC");
            if (right.length() > 0) {
                left = "TC:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //TVR
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "TVR");
            if (right.length() > 0) {
                left = "TVR:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //AID
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "AID");
            if (right.length() > 0) {
                left = "AID:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //TSI
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "TSI");
            if (right.length() > 0) {
                left = "TSI:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //TSI
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "ATC");
            if (right.length() > 0) {
                left = "ATC:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //APPLIB
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "APPLAB");
            if (right.length() > 0) {
                left = "APPLAB:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }

            //APPPN
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "APPPN");
            if (right.length() > 0) {
                left = "APPPN:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }
            printDataFormatter.addLineSeparator()
                    .addCenterAlign().addContent("I AGREE TO PAY ABOVE TOTAL")
                    .addLineSeparator()
                    .addCenterAlign().addContent("AMOUNT ACCORDING TO CARD ISSUER")
                    .addLineSeparator()
                    .addCenterAlign().addContent("AGREEMENT (MERCHANT AGREEMENT")
                    .addLineSeparator()
                    .addCenterAlign().addContent("IF CREDIT VOUCHER)")
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLineSeparator()
                    .addLeftAlign().addContent("X..............................")
                    .addLineSeparator()
                    .addCenterAlign().addContent("SIGNATURE");
        } else if (!response.ResultCode.equals("000000")) {
            printDataFormatter.addCenterAlign().addContent("///////////////////////////////");
            right = response.Message;
            if (right.length() > 0) {
                left = "Response:";
                printDataFormatter.addLeftAlign().addContent(left).addRightAlign().addContent(right)
                        .addLineSeparator();
            }
            printDataFormatter.addCenterAlign().addContent("///////////////////////////////");
        }
        printDataFormatter.addLineSeparator();
        printDataFormatter.addLineSeparator();
        printDataFormatter.addLineSeparator();
        printDataFormatter.addLineSeparator();
        printDataFormatter.addLineSeparator();
        return printDataFormatter.build();
    }
    private String generateReceipt() {
        PaymentResponse response = poslink.PaymentResponse;
        String receiptWidth = "100%";
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            receiptWidth = "40%";
        }
        String content = "<html><body><table width=\"" + receiptWidth + "\" align=\"center\" border=\"0\"><tbody>";

        //time stamp
        String temp = response.Timestamp;
        String left, right;
        if (temp.length() > 0) {
            left = temp.substring(4, 6) + "/" + temp.substring(6, 8) + "/" + temp.substring(0, 4);
            right = temp.substring(8, 10) + ":" + temp.substring(10, 12) + ":" + temp.substring(12, 14);

            content += UIUtil.paddingLine(left, right);
            content += UIUtil.paddingLine("&nbsp;");
        }

        // edcType + transType
        left = mLastRequestTender + " " + mLastRequestTrans + ":";
        content += UIUtil.paddingLine(left, "");
        content += UIUtil.paddingLine("&nbsp;");

        //transaction number;
        left = "Transaction #:";
        right = response.RefNum;
        content += UIUtil.paddingLine(left, right);

        //card Type:
        if (response.CardType.length() > 0) {
            left = "Card Type:";
            right = response.CardType;
            content += UIUtil.paddingLine(left, right);
        }

        //account type
        left = "Account:";
        right = response.BogusAccountNum;
        content += UIUtil.paddingLine(left, right);

        //entry mode
        //left = "Entry:";
        // right =
        left = "Entry";
        temp = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "PLEntryMode");

        if (temp.contains("0")) {
            right = "Manual";
        } else if (temp.contains("1")) {
            right = "Swipe";
        } else if (temp.contains("2")) {
            right = "Contactless";
        } else if (temp.contains("3")) {
            right = "Scanner";
        } else if (temp.contains("4")) {
            right = "Chip";
        } else if (temp.contains("5")) {
            right = "Chip Fall Back Swipe";
        }

        if (temp.length() > 0)
            content += UIUtil.paddingLine(left, right);

        if (response.ResultCode.contains("000000")) {
            //amount
            temp = response.ApprovedAmount;
            if (temp.length() > 0) {
                left = "Amount:";

                int len = temp.length();
                if (len == 2) {
                    right = "$" + "0." + temp;
                } else if (len == 1) {
                    right = "$" + "0.0" + temp;
                } else {
                    right = "$" + temp.substring(0, len - 2) + "." + temp.substring(len - 2);
                }
                content += UIUtil.paddingLine(left, right);
            }

            //order number
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "MOTOECommerceOrderNum");
            if (right.length() > 0) {
                left = "Order Number";
                content += UIUtil.paddingLine(left, right);
            }

            content += UIUtil.paddingLine("&nbsp;");

            //ref
            right = response.HostCode;
            if (right.length() > 0) {
                left = "Ref. Number:";
                content += UIUtil.paddingLine(left, right);
            }

            //VALCODE
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "ValCode");
            if (right.length() > 0) {
                left = "ValCode:";
                content += UIUtil.paddingLine(left, right);
            }

            //auth code
            right = response.AuthCode;
            if (right.length() > 0) {
                left = "Auth Code:";
                content += UIUtil.paddingLine(left, right);
            }

            //response
            right = response.Message;
            if (right.length() > 0) {
                left = "Response:";
                content += UIUtil.paddingLine(left, right);
            }

            //TC
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "TC");
            if (right.length() > 0) {
                left = "TC:";
                content += UIUtil.paddingLine(left, right);
            }

            //TVR
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "TVR");
            if (right.length() > 0) {
                left = "TVR:";
                content += UIUtil.paddingLine(left, right);
            }

            //AID
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "AID");
            if (right.length() > 0) {
                left = "AID:";
                content += UIUtil.paddingLine(left, right);
            }

            //TSI
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "TSI");
            if (right.length() > 0) {
                left = "TSI:";
                content += UIUtil.paddingLine(left, right);
            }

            //TSI
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "ATC");
            if (right.length() > 0) {
                left = "ATC:";
                content += UIUtil.paddingLine(left, right);
            }

            //APPLIB
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "APPLAB");
            if (right.length() > 0) {
                left = "APPLAB:";
                content += UIUtil.paddingLine(left, right);
            }

            //APPPN
            right = UIUtil.findXMl(poslink.PaymentResponse.ExtData, "APPPN");
            if (right.length() > 0) {
                left = "APPPN:";
                content += UIUtil.paddingLine(left, right);
            }

            content += UIUtil.paddingLine("&nbsp;");

            content += UIUtil.paddingLine("I AGREE TO PAY ABOVE TOTAL");
            content += UIUtil.paddingLine("AMOUNT ACCORDING TO CARD ISSUER");
            content += UIUtil.paddingLine("AGREEMENT (MERCHANT AGREEMENT");
            content += UIUtil.paddingLine("IF CREDIT VOUCHER)");
            content += UIUtil.paddingLine("&nbsp;");
            content += UIUtil.paddingLine("X..............................", "");
            content += UIUtil.paddingLine("SIGNATURE");
        } else if (!response.ResultCode.equals("000000")) {
            content += UIUtil.paddingLine("///////////////////////////////");
            right = response.Message;
            if (right.length() > 0) {
                left = "Response:";
                content += UIUtil.paddingLine(left, right);
            }
            content += UIUtil.paddingLine("///////////////////////////////");
        }
        content += "</tbody></table></body></html>";
        return content;
    }

    public static String formatVASData (String[] vasData) {
        if (vasData == null) {
            return "";
        }
        int iMax = vasData.length - 1;
        if (iMax == -1)
            return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; ; i++) {
            b.append(vasData[i]);
            if (i == iMax)
                return b.toString();
            b.append("[1e]");
        }
    }
}

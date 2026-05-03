package com.pax.poslink.ui.report;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.pax.poslink.R;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.ReportResponse;
import com.pax.poslink.SettingINI;
import com.pax.poslink.ui.base.RequestResponseFragment;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.view.TitleItemEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon.F on 2018/1/23.
 */


public class ReportFragment extends RequestResponseFragment<ReportRequest, ReportResponse, ReportRequestFragment> implements View.OnTouchListener, ProcessProgressDialog.OnSetListener{

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case com.pax.poslink.util.Constant.TRANSACTION_SUCCESSED:
                ReportResponse response = (ReportResponse) msg.obj;
                setReportResponse(response);
                break;
            case com.pax.poslink.util.Constant.TRANSACTION_TIMEOOUT:
            case com.pax.poslink.util.Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(com.pax.poslink.util.Constant.DIALOG_TITLE);
                String message = msg.getData().getString(com.pax.poslink.util.Constant.DIALOG_MESSAGE);
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
        }
    }

    public static ReportFragment newInstance() {
        return new ReportFragment();
    }

    @Override
    protected ReportRequestFragment createRequestFragment() {
        return ReportRequestFragment.newInstance();
    }

    @Override
    protected ReportResponse getResponse() {
        return poslink.ReportResponse;
    }

    @Override
    public void onPreRequest(ReportRequest request) {
        if (responseLayout.getVisibility() == View.VISIBLE)
            initReportResponse();
        super.onPreRequest(request);
    }

    @Override
    public void run() {
        // processTransactions
        Log.i(TAG, "ReportRequest.TransType = " + request.TransType);
        // set the folder to save the "comsetting.ini" file
        poslink.appDataFolder = getContext().getFilesDir().getAbsolutePath();
        poslink.SetCommSetting(SettingINI.getCommSettingFromFile(poslink.appDataFolder + "/" + SettingINI.FILENAME));

        poslink.ReportRequest = request;
        // ProcessTrans is Blocking call, will return when the transaction is
        // complete.
        ptr = poslink.ProcessTrans();
    }

    private void setReportResponse(ReportResponse response) {
        if (responseLayout.getVisibility() == View.GONE) {
            responseLayout.setVisibility(View.VISIBLE);
        }
        responseRenderEntityList.clear();
        responseContainer.removeAllViews();
        Resources resources = getResources();

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_resultCode), response.ResultCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_resultTxt), response.ResultTxt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_edcType), response.EDCType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_totalRecord), response.TotalRecord));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_recordNumber), response.RecordNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_paymentType), response.PaymentType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_origPaymentType), response.OrigPaymentType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_hostTraceNum), response.HostTraceNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_batchNum), response.BatchNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_authCode), response.AuthCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_hostCode), response.HostCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_hostResponse), response.HostResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_message), response.Message));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_approvedAmount), response.ApprovedAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedTipAmt), response.ApprovedTipAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedCashBackAmount), response.ApprovedCashBackAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedMerchantFee), response.ApprovedMerchantFee));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_approvedTaxAmount), response.ApprovedTaxAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_remainingBalance), response.RemainingBalance));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_extraBalance), response.ExtraBalance));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_bogusAccountNum), response.BogusAccountNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_cardType), response.CardType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_cvResponse), response.CvResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_refNum), response.RefNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_transactionIdentifier), response.TransactionIdentifier));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_ecrRefNum), response.ECRRefNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_timestamp), response.Timestamp));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_request_invNum), response.InvNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_clerkId), response.ClerkID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_shiftId), response.ShiftID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_reportType), response.ReportType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_creditCount), response.CreditCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_creditAmount), response.CreditAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_debitCount), response.DebitCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_debitAmount), response.DebitAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_ebtCount), response.EBTCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_ebtAmount), response.EBTAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_giftCount), response.GiftCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_giftAmount), response.GiftAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_loyaltyCount), response.LoyaltyCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_loyaltyAmount), response.LoyaltyAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_cashCount), response.CashCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_cashAmount), response.CashAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_checkCount), response.CHECKCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_checkAmount), response.CHECKAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_extData), response.ExtData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_transTotal), response.TransTotal));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_gift_card_type), response.GiftCardType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_ext_debit_account_type), response.DebitAccountType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_tic), response.TransactionIntegrityClass));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_transaction_remaining_amount), response.TransactionRemainingAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_gateway_id), response.GatewayTransactionID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_host_detail_message), response.HostDetailedMessage));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_ps2000), response.PaymentService2000));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_authorization_response), response.AuthorizationResponse));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_issuer_response_code), response.IssuerResponseCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_ecr_transaction_id), response.ECRTransID));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payment_response_accountReferenceID), response.PaymentAccountReferenceID));

        responseRenderEntityList.add(new TitleItemEntity("SAF Information"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_VisaCnt), response.VisaCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_VisaAmount), response.VisaAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_MCCnt), response.MasterCardCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_MCAmount), response.MasterCardAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_AmexCnt), response.AMEXCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_AmexAmount), response.AMEXAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_DinersCnt), response.DinersCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_DinersAmount), response.DinersAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_DiscoverCnt), response.DiscoverCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_DiscoverAmount), response.DiscoverAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_JCBCnt), response.JCBCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_JCBAmount), response.JCBAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_enRouteCnt), response.enRouteCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_enRouteAmount), response.enRouteAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_ExtendedCnt), response.ExtendedCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_ExtendedAmount), response.ExtendedAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_VisafleetAmount), response.VisaFleetAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_VisafleetCnt), response.VisaFleetCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_MCfleetCnt), response.MasterCardFleetCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_MCfleetAmount), response.MasterCardFleetAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_fleetoneCnt), response.FleetOneCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_fleetoneAmount), response.FleetOneAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_fleetwideCnt), response.FleetwideCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_fleetwideAmount), response.FleetwideAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_fuelmanCnt), response.FuelmanCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_fuelmanAmount), response.FuelmanAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_gascardCnt), response.GascardCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_gascardAmount), response.GascardAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_voyagerCnt), response.VoyagerCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_voyagerAmount), response.VoyagerAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_wrightexpressCnt), response.WrightExpressCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_wrightexpressAmount), response.WrightExpressAmount));

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_interacCount), response.InteracCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_interacAmount), response.InteracAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_CUPCount), response.CUPCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_CUPAmount), response.CUPAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_maestroCount), response.MaestroCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_maestroAmount), response.MaestroAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_sinclairCount), response.SinclairCount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_sinclairAmount), response.SinclairAmount));

        responseRenderEntityList.add(new TitleItemEntity("TOR Info"));
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

        responseRenderEntityList.add(new TitleItemEntity("AddlRsp"));
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

        responseRenderEntityList.add(new TitleItemEntity("Transaction information"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("DiscountAmount", response.ReportTransInfo.DiscountAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ChargedAmount", response.ReportTransInfo.ChargedAmount));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SignatureStatus", response.ReportTransInfo.SignatureStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Fps", response.ReportTransInfo.Fps));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("FpsSignature", response.ReportTransInfo.FpsSignature));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("FpsReceipt", response.ReportTransInfo.FpsReceipt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("token", response.ReportTransInfo.Token));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("HRef", response.ReportTransInfo.HRef));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SN", response.ReportTransInfo.SN));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("SettlementDate", response.ReportTransInfo.SettlementDate));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("HostEchoData", response.ReportTransInfo.HostEchoData));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("PinStatusNum", response.ReportTransInfo.PinStatusNum));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ValidationCode", response.ReportTransInfo.ValidationCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("UserLanguageStatus", response.ReportTransInfo.UserLanguageStatus));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("GlobalUid", response.ReportTransInfo.GlobalUid));

        responseRenderEntityList.add(new TitleItemEntity("EMV Tag"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TC", response.ReportEmvTag.Tc));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TVR", response.ReportEmvTag.Tvr));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AID", response.ReportEmvTag.Aid));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TSI", response.ReportEmvTag.Tsi));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ATC", response.ReportEmvTag.Atc));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("APPLAB", response.ReportEmvTag.AppLabel));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("APPPN", response.ReportEmvTag.AppPreferName));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("IAD", response.ReportEmvTag.Iad));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ARC", response.ReportEmvTag.Arc));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CID", response.ReportEmvTag.Cid));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("CVM", response.ReportEmvTag.Cvm));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ARQC", response.ReportEmvTag.Arqc));

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

        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Hubometer", response.FleetCard.Hubometer));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MainTenanceId", response.FleetCard.MainTenanceId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("FleetPoNumber", response.FleetCard.FleetPoNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("ReeferHours", response.FleetCard.ReeferHours));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TrailerId", response.FleetCard.TrailerId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("TripNumber", response.FleetCard.TripNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("UnitId", response.FleetCard.UnitId));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AdditionalFleetData1", response.FleetCard.AdditionalFleetData1));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AdditionalFleetData2", response.FleetCard.AdditionalFleetData2));

        responseRenderEntityList.add(new TitleItemEntity("AVS Info"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AVS approval Code", response.AVSInformation.AVSApprovalCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("AVS message ", response.AVSInformation.AVSMessage));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Zip Code", response.AVSInformation.ZipCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Address 1", response.AVSInformation.Address));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Address 2", response.AVSInformation.Address2));

        responseRenderEntityList.add(new TitleItemEntity("MOTO/E-commerce"));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("MOTO/e-Commerce mode", response.MOTOECommerce.MOTOECommerceMode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Transaction type", response.MOTOECommerce.TransactionType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Secure type", response.MOTOECommerce.SecureType));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Order number", response.MOTOECommerce.OrderNumber));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Installments", response.MOTOECommerce.Installments));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity("Current installment", response.MOTOECommerce.CurrentInstallment));

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

    private void initReportResponse() {
        setReportResponse(new ReportResponse());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
        //don't nee to set a listener
    }

    @Override
    public Dialog createDialog() {
        return ProcessProgressDialog.createDialog(getActivity(), getResources().getString(R.string.report_process_prompt), false, false, this);
    }

}

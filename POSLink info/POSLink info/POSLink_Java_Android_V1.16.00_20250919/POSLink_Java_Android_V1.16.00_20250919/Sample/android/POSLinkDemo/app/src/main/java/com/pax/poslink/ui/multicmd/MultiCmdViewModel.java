package com.pax.poslink.ui.multicmd;

import androidx.lifecycle.ViewModel;

import com.pax.poslink.BatchRequest;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.PayloadRequest;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.ReportRequest;

/**
 * @author Justin.Z on 2021-7-20
 */
public class MultiCmdViewModel extends ViewModel {

    private ManageRequest mManageRequest;
    private ReportRequest mReportRequest;
    private BatchRequest mBatchRequest;
    private PaymentRequest mPaymentRequest;
    private PayloadRequest mPayloadRequest;

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public void setManageRequest(ManageRequest manageRequest) {
        this.mManageRequest = manageRequest;
    }

    public ManageRequest getManageRequest() {
        return mManageRequest == null ? new ManageRequest() : mManageRequest;
    }

    public ReportRequest getReportRequest() {
        return mReportRequest == null ? new ReportRequest() : mReportRequest;
    }

    public void setReportRequest(ReportRequest mReportRequest) {
        this.mReportRequest = mReportRequest;
    }

    public BatchRequest getBatchRequest() {
        return mBatchRequest == null ? new BatchRequest() : mBatchRequest;
    }

    public void setBatchRequest(BatchRequest mBatchRequest) {
        this.mBatchRequest = mBatchRequest;
    }

    public PaymentRequest getPaymentRequest() {
        return mPaymentRequest == null ? new PaymentRequest() : mPaymentRequest;
    }

    public void setPaymentRequest(PaymentRequest mPaymentRequest) {
        this.mPaymentRequest = mPaymentRequest;
    }

    public PayloadRequest getPayloadRequest() {
        return mPayloadRequest == null ? new PayloadRequest() : mPayloadRequest;
    }

    public void setPayloadRequest(PayloadRequest mPayloadRequest) {
        this.mPayloadRequest = mPayloadRequest;
    }
}

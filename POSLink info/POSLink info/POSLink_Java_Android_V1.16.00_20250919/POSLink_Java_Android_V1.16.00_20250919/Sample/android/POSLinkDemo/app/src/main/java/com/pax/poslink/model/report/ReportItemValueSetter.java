package com.pax.poslink.model.report;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.pax.poslink.ReportRequest;
import com.pax.poslink.entity.MultiMerchant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon.F on 2018/1/15.
 */
public interface ReportItemValueSetter {
    String TRANS_TYPE = "TRANS Type";
    String EDC_TYPE = "EDC Type";
    String EXT_DATA = "Ext Data";
    String SAF_INDICATOR = "SAF Indicator";
    String PAYMENT_TYPE = "Payment Type";
    String CARD_TYPE = "Card Type";
    String RECORD_NUM = "Record Num";
    String REF_NUM = "Ref Num";
    String AUTH_CODE = "Auth Code";
    String ECR_REF_NUM = "ECR Ref Num";
    String LAST_TRANSACTION = "Last Transaction";
    String MULTI_MERCHANT = "Multi Merchant";
    String ECR_TRANS_ID = "ECR Trans Id";
    String HREF = "HRefNum";
    String TRANSACTION_RESULT_TYPE = "Transaction Result Type";
    Map<String, ReportItemValueSetter> VALUE_SETTER_MAP = new HashMap<String, ReportItemValueSetter>() {
        {

            put(EDC_TYPE, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.EDCType = request.ParseEDCType(value);
                }
            });


            put(EXT_DATA, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.ExtData = value;
                }
            });


            put(SAF_INDICATOR, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.SAFIndicator = value;
                }
            });

            put(PAYMENT_TYPE, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.PaymentType = request.ParsePaymentType(value);
                }
            });

            put(CARD_TYPE, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.CardType = request.ParseCardType(value);
                }
            });

            put(RECORD_NUM, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.RecordNum = value;
                }
            });

            put(REF_NUM, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.RefNum = value;
                }
            });

            put(AUTH_CODE, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.AuthCode = value;
                }
            });

            put(ECR_REF_NUM, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.ECRRefNum = value;
                }
            });
            put(LAST_TRANSACTION, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.LastTransaction = value;
                }
            });
            put(MULTI_MERCHANT, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.MultiMerchant = gson.fromJson(value, MultiMerchant.class);
                    }
                }
            });
            put(ECR_TRANS_ID, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.ECRTransID = value;
                }
            });
            put(HREF, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.HRefNum = value;
                }
            });
            put(TRANSACTION_RESULT_TYPE, new ReportItemValueSetter() {
                @Override
                public void onSet(ReportRequest request, String value) {
                    request.TransactionResultType = value;
                }
            });
        }
    };

    void onSet(ReportRequest request, String value);
}

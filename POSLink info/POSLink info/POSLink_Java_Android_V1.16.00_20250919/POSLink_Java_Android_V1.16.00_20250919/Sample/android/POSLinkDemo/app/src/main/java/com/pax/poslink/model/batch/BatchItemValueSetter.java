package com.pax.poslink.model.batch;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.pax.poslink.BatchRequest;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.entity.MultiMerchant;
import com.pax.poslink.model.manage.ManageItemValueSetter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon.F on 2018/1/15.
 */
public interface BatchItemValueSetter {
    String EDC_TYPE = "EDC Type";
    String EXT_DATA = "Ext Data";
    String TIMESTAMP = "Timestamp";
    String SAF_INDICATOR = "SAF Indicator";
    String PAYMENT_TYPE = "Payment Type";
    String CARD_TYPE = "Card Type";
    String RECORD_NUM = "Record Num";
    String REF_NUM = "Ref Num";
    String AUTH_CODE = "Auth Code";
    String ECR_REF_NUM = "ECR Ref Num";
    String MULTI_MERCHANT = "Multi Merchant";

    Map<String, BatchItemValueSetter> VALUE_SETTER_MAP = new HashMap<String, BatchItemValueSetter>() {
        {

            put(EDC_TYPE, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.EDCType = request.ParseEDCType(value);
                }
            });


            put(EXT_DATA, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.ExtData = value;
                }
            });

            put(TIMESTAMP, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.Timestamp = value;
                }
            });

            put(SAF_INDICATOR, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.SAFIndicator = value;
                }
            });

            put(PAYMENT_TYPE, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.PaymentType = request.ParsePaymentType(value);
                }
            });

            put(CARD_TYPE, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.CardType = request.ParseCardType(value);
                }
            });

            put(RECORD_NUM, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.RecordNum = value;
                }
            });

            put(REF_NUM, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.RefNum = value;
                }
            });

            put(AUTH_CODE, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.AuthCode = value;
                }
            });

            put(ECR_REF_NUM, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    request.ECRRefNum = value;
                }
            });

            put(MULTI_MERCHANT, new BatchItemValueSetter() {
                @Override
                public void onSet(BatchRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.MultiMerchant = gson.fromJson(value, MultiMerchant.class);
                    }
                }
            });
        }
    };

    void onSet(BatchRequest request, String value);
}

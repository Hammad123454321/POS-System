package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-5-25
 */
public interface PaymentOriginalValueSetter {

    String ORIGTRANSDATE = "TransDate";
    String ORIGPAN = "Pan";
    String ORIGEXPIRYDATE = "ExpiryDate";
    String ORIGTRANSTIME = "TransTime";
    String ORIGSETTLEMENTDATE = "SettlementDate";
    String ORIGTRANSTYPE = "TransType";
    String ORIGAMOUNT = "Amount";
    String ORIGTRANSID= "TransId";
    String ORIGBATCHNUMBER = "BatchNumber";
    String PS2000 = "PaymentService 2000";
    String AUTH_RESPONSE = "Authorization Response";
    String TRANSACTIONIDENTIFIER = "Transaction Identifier";

    Map<String, PaymentOriginalValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentOriginalValueSetter>() {
        {
            put(ORIGTRANSDATE, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.TransDate = value;
                }
            });
            put(ORIGPAN, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.Pan = value;
                }
            });
            put(ORIGEXPIRYDATE, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.ExpiryDate = value;
                }
            });
            put(ORIGTRANSTIME, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.TransTime = value;
                }
            });
            put(ORIGSETTLEMENTDATE, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.SettlementDate = value;
                }
            });
            put(ORIGTRANSTYPE, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.TransType = value;
                }
            });
            put(ORIGAMOUNT, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.Amount = value;
                }
            });

            put(ORIGTRANSID, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.TransId = value;
                }
            });
            put(ORIGBATCHNUMBER, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.BatchNumber = value;
                }
            });
            put(PS2000, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.PaymentService2000 = value;
                }
            });
            put(AUTH_RESPONSE, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.AuthorizationResponse = value;
                }
            });
            put(TRANSACTIONIDENTIFIER, new PaymentOriginalValueSetter() {
                @Override
                public void onSet(PaymentRequest.Original original, String value) {
                    original.TransactionIdentifier = value;
                }
            });
        }
    };


    void onSet(PaymentRequest.Original original, String value);
}

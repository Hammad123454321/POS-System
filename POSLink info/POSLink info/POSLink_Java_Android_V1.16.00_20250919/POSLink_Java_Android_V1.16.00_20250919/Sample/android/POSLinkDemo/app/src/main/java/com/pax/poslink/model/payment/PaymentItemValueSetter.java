package com.pax.poslink.model.payment;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.entity.FleetCardRequest;
import com.pax.poslink.entity.MultiMerchant;
import com.pax.poslink.entity.Restaurant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon.F on 2018/1/19.
 */

public interface PaymentItemValueSetter {

    String EDC_TYPE = "EDC Type";
    String TRANS_TYPE = "TRANS TYPE";
    String EXT_DATA = "Ext Data";

    String AMOUNT = "Amount";
    String CASH_BACK_AMOUNT = "Cash Back Amt";
    String CLERK_ID = "Clerk ID";
    String SIG_SAVE_PATH = "Sig Save Path";
    String ZIP = "Zip";
    String TIP_AMT = "Tip Amt";
    String TAX_AMT = "Tax Amt";
    String FUEL_AMT = "Fuel Amt";
    String STREET = "Street";
    String STREET2 = "Street2";
    String SURCHARGE_AMT = "Surcharge Amt";
    String PO_NUM = "PO Num";
    String ORIG_REF_NUM = "Orig Ref Num";
    String INV_NUM = "Inv Num";
    String ECR_REF_NUM = "ECR Ref Num";
    String ORIG_ECR_REF_NUM = "Orig ECR Ref Num";
    String ECR_Trans_ID = "ECR Trans ID";
    String AUTH_CODE = "Auth Code";
    String SERVICE_FEE = "Service Fee";
    String CONTINUOUS_SCREEN = "ContinuousScreen";
    String COMMERCIALCARD = "CommercialCard";
    String RESTAURANT = "Restaurant";
    String HOST_GATEWAY = "Host/GateWay";
    String TRANSACTION_BEHAVIOR = "Transaction Behavior";
    String ORIGINAL = "Original";
    String FLEET_CARD = "Fleet Card";
    String MULTI_MERCHANT = "Multi Merchant";
    String GIFTCARDTYPE = "Gift Card Type";
    String LODGINGINFO = "Lodging Info";
    String AUTORENTALINFO = "Auto Rental Info";
    String CVVBYPASSREASON = "CVV Bypass Reason";
    String GIFTTENDERTYPE = "Gift Tender Type";
    String TRACENUMBER = "Trace Number";
    String HOSTCREDENTIALINFORMATION = "Host Credential Information";
    String PAYLOAD_DATA = "Payload Data";

//    String AID_FILTER_RULE = "AID Filter Rule";


    Map<String, PaymentItemValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentItemValueSetter>() {
        {

            put(EDC_TYPE, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.TenderType = request.ParseTenderType(value);
                }
            });

            put(TRANS_TYPE, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.TransType = request.ParseTransType(value);
                }
            });


            put(EXT_DATA, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.ExtData = value;
                }
            });



            put(AMOUNT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.Amount = value;
                }
            });
            put(CASH_BACK_AMOUNT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.CashBackAmt = value;
                }
            });
            put(CLERK_ID, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.ClerkID = value;
                }
            });
            put(SIG_SAVE_PATH, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                }
            });
            put(ZIP, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.Zip = value;
                }
            });
            put(TIP_AMT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.TipAmt = value;
                }
            });
            put(TAX_AMT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.TaxAmt = value;
                }
            });
            put(FUEL_AMT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.FuelAmt = value;
                }
            });
            put(STREET, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.Street = value;
                }
            });
            put(STREET2, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.Street2 = value;
                }
            });
            put(SURCHARGE_AMT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.SurchargeAmt = value;
                }
            });
            put(PO_NUM, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.PONum = value;
                }
            });
            put(ORIG_REF_NUM, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.OrigRefNum = value;
                }
            });
            put(INV_NUM, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.InvNum = value;
                }
            });
            put(ECR_REF_NUM, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.ECRRefNum = value;
                }
            });
            put(ORIG_ECR_REF_NUM, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.OrigECRRefNum = value;
                }
            });
            put(ECR_Trans_ID, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.ECRTransID = value;
                }
            });
            put(AUTH_CODE, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.AuthCode = value;
                }
            });
            put(CONTINUOUS_SCREEN, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.ContinuousScreen = value;
                }
            });
            put(SERVICE_FEE, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.ServiceFee = value;
                }
            });
            put(GIFTCARDTYPE, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.GiftCardType = value;
                }
            });
            put(CVVBYPASSREASON, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.CVVBypassReason = value;
                }
            });
            put(GIFTTENDERTYPE, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.GiftTenderType = value;
                }
            });
            put(TRACENUMBER, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.OrigTraceNum = value;
                }
            });
//            put(AID_FILTER_RULE, new PaymentItemValueSetter() {
//                @Override
//                public void onSet(PaymentRequest request, String value) {
//                    request.AIDFilterRule = value;
//                }
//            });
            put(COMMERCIALCARD, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.CommercialCard = gson.fromJson(value, PaymentRequest.CommercialCard.class);
                    }
                }
            });
            put(RESTAURANT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.Restaurant = gson.fromJson(value, Restaurant.class);
                    }
                }
            });
            put(HOST_GATEWAY, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.HostGateWay = gson.fromJson(value, PaymentRequest.HostGateWay.class);
                    }
                }
            });
            put(TRANSACTION_BEHAVIOR, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.TransactionBehavior = gson.fromJson(value, PaymentRequest.TransactionBehavior.class);
                    }
                }
            });
            put(ORIGINAL, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.Original = gson.fromJson(value, PaymentRequest.Original.class);
                    }
                }
            });
            put(FLEET_CARD, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.FleetCard = gson.fromJson(value, FleetCardRequest.class);
                    }
                }
            });
            put(MULTI_MERCHANT, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.MultiMerchant = gson.fromJson(value, MultiMerchant.class);
                    }
                }
            });
            put(LODGINGINFO, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.LodgingInfo = gson.fromJson(value, PaymentRequest.LodgingInfo.class);
                    }
                }
            });
            put(AUTORENTALINFO, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.AutoRentalInfo = gson.fromJson(value, PaymentRequest.AutoRentalInfo.class);
                    }
                }
            });
            put(HOSTCREDENTIALINFORMATION, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        request.HostCredentialInformation = gson.fromJson(value, PaymentRequest.HostCredentialInformation.class);
                    }
                }
            });
            put(PAYLOAD_DATA, new PaymentItemValueSetter() {
                @Override
                public void onSet(PaymentRequest request, String value) {
                    request.PayloadData = value;
                }
            });

        }
    };

    void onSet(PaymentRequest request, String value);
}

package com.pax.poslink.model.payment;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Justin.Z on 2021-3-2
 */
public interface PaymentLodgingInfoValueSetter {

    String ROOMNUMBER = "RoomNumber";
    String FOLIONUMBER = "FolioNumber";
    String ROOMRATES = "RoomRates";
    String CHARGETYPE = "ChargeType";
    String NOSHOWFLAG = "NoShowFlag";
    String CHECKINDATE = "CheckInDate";
    String CHECKOUTDATE = "CheckOutDate";
    String SPECIALPROGRAMCODE= "SpecialProgramCode";
    String DEPARTUREADJUSTEDAMOUNT = "DepartureAdjustedAmount";
    String LODGINGITEMS = "LodgingItems";

    Map<String, PaymentLodgingInfoValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentLodgingInfoValueSetter>() {
        {
            put(ROOMNUMBER, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.RoomNumber = value;
                }
            });

            put(FOLIONUMBER, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.FolioNumber = value;
                }
            });

            put(ROOMRATES, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        lodgingInfo.RoomRates = gson.fromJson(value, new TypeToken<List<PaymentRequest.LodgingInfo.RoomRates>>(){}.getType());
                    }
                }
            });
            put(CHARGETYPE, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.ChargeType = value;
                }
            });
            put(NOSHOWFLAG, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.NoShowFlag = value;
                }
            });
            put(CHECKINDATE, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.CheckInDate = value;
                }
            });
            put(CHECKOUTDATE, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.CheckOutDate = value;
                }
            });
            put(SPECIALPROGRAMCODE, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.SpecialProgramCode = value;
                }
            });
            put(DEPARTUREADJUSTEDAMOUNT, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    lodgingInfo.DepartureAdjustedAmount = value;
                }
            });
            put(LODGINGITEMS, new PaymentLodgingInfoValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value) {
                    if (!TextUtils.isEmpty(value)) {
                        Gson gson = new Gson();
                        lodgingInfo.LodgingItems = gson.fromJson(value, new TypeToken<List<PaymentRequest.LodgingInfo.LodgingItem>>(){}.getType());
                    }
                }
            });
        }
    };


    void onSet(PaymentRequest.LodgingInfo lodgingInfo, String value);
}

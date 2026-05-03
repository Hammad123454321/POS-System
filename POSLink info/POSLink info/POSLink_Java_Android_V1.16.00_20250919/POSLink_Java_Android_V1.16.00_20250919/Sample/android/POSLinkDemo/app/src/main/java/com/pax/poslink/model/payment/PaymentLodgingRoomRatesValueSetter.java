package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Justin.Z on 2021-3-2
 */
public interface PaymentLodgingRoomRatesValueSetter {

    String STAYDURATION = "StayDuration";
    String ROOMRATEAMOUNT = "RoomRateAmount";
    String ROOMRATETAX = "RoomRateTax";

    Map<String, PaymentLodgingRoomRatesValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentLodgingRoomRatesValueSetter>() {
        {
            put(STAYDURATION, new PaymentLodgingRoomRatesValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo.RoomRates roomRates, String value) {
                    roomRates.StayDuration = value;
                }

            });
            put(ROOMRATEAMOUNT, new PaymentLodgingRoomRatesValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo.RoomRates roomRates, String value) {
                    roomRates.RoomRateAmount = value;
                }

            });
            put(ROOMRATETAX, new PaymentLodgingRoomRatesValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo.RoomRates roomRates, String value) {
                    roomRates.RoomRateTax = value;
                }

            });
        }
    };

    void onSet(PaymentRequest.LodgingInfo.RoomRates roomRates, String value);
}

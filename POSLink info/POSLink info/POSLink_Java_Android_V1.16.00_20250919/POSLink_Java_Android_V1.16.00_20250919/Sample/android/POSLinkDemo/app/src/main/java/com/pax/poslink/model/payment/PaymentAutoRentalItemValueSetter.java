package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Justin.Z on 2021-7-14
 */
public interface PaymentAutoRentalItemValueSetter {

    String ITEMTYPE = "ItemType";
    String EXTRACHARGEAMOUNT = "ExtraChargeAmount";

    Map<String, PaymentAutoRentalItemValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentAutoRentalItemValueSetter>() {
        {
            put(ITEMTYPE, new PaymentAutoRentalItemValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo.ExtraChargeItem extraChargeItem, String value) {
                    extraChargeItem.ItemType = value;
                }
            });
            put(EXTRACHARGEAMOUNT, new PaymentAutoRentalItemValueSetter() {

                @Override
                public void onSet(PaymentRequest.AutoRentalInfo.ExtraChargeItem extraChargeItem, String value) {
                    extraChargeItem.ExtraChargeAmount = value;
                }
            });
        }
    };

    void onSet(PaymentRequest.AutoRentalInfo.ExtraChargeItem extraChargeItem, String value);
}

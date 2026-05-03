package com.pax.poslink.model.payment;

import com.pax.poslink.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Justin.Z on 2021-3-3
 */
public interface PaymentLodgingItemsValueSetter {

    String ITEMTYPE = "ItemType";
    String ITEMAMOUNT = "ITEMAMOUNT";
    String ITEMCODE = "ITEMCODE";

    Map<String, PaymentLodgingItemsValueSetter> VALUE_SETTER_MAP = new HashMap<String, PaymentLodgingItemsValueSetter>() {
        {
            put(ITEMTYPE, new PaymentLodgingItemsValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo.LodgingItem lodgingItem, String value) {
                    lodgingItem.ItemType = value;
                }

            });
            put(ITEMAMOUNT, new PaymentLodgingItemsValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo.LodgingItem lodgingItem, String value) {
                    lodgingItem.ItemAmount = value;
                }

            });
            put(ITEMCODE, new PaymentLodgingItemsValueSetter() {

                @Override
                public void onSet(PaymentRequest.LodgingInfo.LodgingItem lodgingItem, String value) {
                    lodgingItem.ItemCode = value;
                }
            });
        }
    };

    void onSet(PaymentRequest.LodgingInfo.LodgingItem lodgingItem, String value);
}

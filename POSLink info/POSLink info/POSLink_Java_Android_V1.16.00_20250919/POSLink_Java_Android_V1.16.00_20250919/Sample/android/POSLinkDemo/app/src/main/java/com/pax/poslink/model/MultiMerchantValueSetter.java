package com.pax.poslink.model;

import com.pax.poslink.entity.MultiMerchant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-5-26
 */
public interface MultiMerchantValueSetter {

    String MM_ID = "MM_ID";
    String MM_NAME = "MM_NAME";

    Map<String, MultiMerchantValueSetter> VALUE_SETTER_MAP = new HashMap<String, MultiMerchantValueSetter>() {
        {
            put(MM_ID, new MultiMerchantValueSetter() {
                @Override
                public void onSet(MultiMerchant multiMerchant, String value) {
                    multiMerchant.Id = value;
                }
            });
            put(MM_NAME, new MultiMerchantValueSetter() {
                @Override
                public void onSet(MultiMerchant multiMerchant, String value) {
                    multiMerchant.Name = value;
                }
            });
        }
    };

    void onSet(MultiMerchant multiMerchant, String value);
}

package com.pax.poslink.model.payload;

import com.pax.poslink.PayloadRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Justin.Z on 2021-7-15
 */
public interface PayloadValueSetter {

    String PAYLOAD = "Payload";

    Map<String, PayloadValueSetter> VALUE_SETTER_MAP = new HashMap<String, PayloadValueSetter>() {
        {
            put(PAYLOAD, new PayloadValueSetter() {
                @Override
                public void onSet(PayloadRequest request, String value) {
                    request.Payload = value;
                }
            });
        }};

    void onSet(PayloadRequest request, String value);
}

package com.pax.poslink.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon.F on 2018/1/12.
 */

public class ExtDataEntity extends NameValueStringEntity {

    public ExtDataEntity(String name, String value, int inputType, String hint) {
        super(name, value, inputType, hint);
    }

    public ExtDataEntity(String name, String value, int inputType, String hint, ClickCallback clickCallback) {
        super(name, value, inputType, hint, clickCallback);
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        return new ExtDataView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_button_ext_data, parent, false));
    }
}

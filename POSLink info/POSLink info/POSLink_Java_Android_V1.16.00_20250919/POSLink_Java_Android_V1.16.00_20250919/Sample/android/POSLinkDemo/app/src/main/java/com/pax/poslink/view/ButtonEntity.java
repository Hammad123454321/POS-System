package com.pax.poslink.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

public class ButtonEntity extends NameValueStringEntity {

    public ButtonEntity(String name, String value, int inputType, String hint) {
        super(name, value, inputType, hint);
    }

    public ButtonEntity(String name, String value, int inputType, String hint, ClickCallback clickCallback) {
        super(name, value, inputType, hint, clickCallback);
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        return new ExtDataView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_button_uneditable, parent, false));
    }
}

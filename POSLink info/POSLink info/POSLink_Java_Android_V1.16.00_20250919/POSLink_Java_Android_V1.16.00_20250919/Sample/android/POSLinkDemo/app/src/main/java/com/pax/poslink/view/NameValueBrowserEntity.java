package com.pax.poslink.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon.F on 2018/1/12.
 */

public class NameValueBrowserEntity extends NameValueStringEntity {
    private String buttonName = "Browse";

    public NameValueBrowserEntity(String name, String value, int inputType, String hint) {
        super(name, value, inputType, hint);
    }

    public NameValueBrowserEntity(String name, String buttonName, String value, int inputType, String hint, ClickCallback clickCallback) {
        super(name, value, inputType, hint, clickCallback);
        this.buttonName = buttonName;
    }

    public String getButtonName() {
        return buttonName;
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        return new NameValueBrowserView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_name_string_browser, parent, false));
    }
}

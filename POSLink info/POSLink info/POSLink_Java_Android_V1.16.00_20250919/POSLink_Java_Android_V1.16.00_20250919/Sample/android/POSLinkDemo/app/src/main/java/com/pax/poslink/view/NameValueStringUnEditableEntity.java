package com.pax.poslink.view;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon.F on 2018/1/30.
 */

public class NameValueStringUnEditableEntity extends NameValueStringEntity {

    private String value;

    public NameValueStringUnEditableEntity(String name, String value) {
        super(name, value, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "");
        this.value = value;
    }

    public NameValueStringUnEditableEntity(String name, String value, ClickCallback clickCallback) {
        super(name, value, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE, "", clickCallback);
        this.value = value;
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        return new NameValueItemStringView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_name_string_uneditable, parent, false));
    }

    public String getValue() {
        return value;
    }
}

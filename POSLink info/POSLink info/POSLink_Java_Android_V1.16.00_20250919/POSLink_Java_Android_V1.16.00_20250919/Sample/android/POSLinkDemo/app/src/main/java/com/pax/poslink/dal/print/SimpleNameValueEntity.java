package com.pax.poslink.dal.print;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pax.poslink.R;


/**
 * Created by Leon on 2017/12/4.
 */

public class SimpleNameValueEntity {
    private String name;
    private String value;

    public SimpleNameValueEntity(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public SimpleNameValueItemView createView(ViewGroup parent) {
        return new SimpleNameValueItemView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_print_name_value, parent, false));
    }
}

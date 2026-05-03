package com.pax.poslink.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon.F on 2018/1/12.
 */

public class NameStringWithUnitEntity extends NameValueStringEntity {

    private String unitStr = "0.1s";

    public NameStringWithUnitEntity(String name, String value, int inputType, String hint) {
        super(name, value, inputType, hint);
    }

    public NameStringWithUnitEntity(String name, String value, int inputType, String hint, String unitStr) {
        super(name, value, inputType, hint);
        this.unitStr = unitStr;
    }

    public String getUnitStr() {
        return unitStr;
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        return new NameValueStringUnitView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_name_string_unit, parent, false));
    }
}

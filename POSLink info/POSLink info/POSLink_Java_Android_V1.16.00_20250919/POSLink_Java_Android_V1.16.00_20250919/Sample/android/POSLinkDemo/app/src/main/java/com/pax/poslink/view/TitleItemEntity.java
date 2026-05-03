package com.pax.poslink.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;

/**
 * Created by Leon.F on 2018/1/23.
 */

public class TitleItemEntity implements RenderEntity {

    private String name;

    public TitleItemEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        return new TitleItemView(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
    }
}

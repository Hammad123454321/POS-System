package com.pax.poslink.ui.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;

/**
 * Created by Leon on 2017/6/21.
 */

public class TabItemEntity implements RenderEntity{

    private String title;
    private OnItemClickCallback onClickListener;
    private boolean isSelected = false;

    public TabItemEntity(String title, OnItemClickCallback onClickListener) {
        this.title = title;
        this.onClickListener = onClickListener;
    }

    public String getTitle() {
        return title;
    }

    public OnItemClickCallback getOnClickListener() {
        return onClickListener;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab, parent, false);
        return new TabItemView(view);
    }

    public interface OnItemClickCallback {
        void onClick(View v, TabItemEntity renderEntity);
    }
}

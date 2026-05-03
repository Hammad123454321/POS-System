
package com.pax.poslink.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

import java.util.List;

public class NameValueSelectEntity extends NameValueEntity<String> {

    private List<String> itemNames;
    private List<String> itemValues;
    private int selectedItem;
    private OnSelectCallback onSelectCallback;

    public NameValueSelectEntity(String name, String value, List<String> itemNames, List<String> itemValues) {
        super(name, value);
        this.itemNames = itemNames;
        this.itemValues = itemValues;
    }

    public NameValueSelectEntity(String name, String value, List<String> itemNames, List<String> itemValues, int selectedItem) {
        super(name, value);
        this.itemNames = itemNames;
        this.itemValues = itemValues;
        this.selectedItem = selectedItem;
    }

    public NameValueSelectEntity(String name, String value, List<String> itemNames, List<String> itemValues, int selectedItem, OnSelectCallback onSelectCallback) {
        super(name, value);
        this.itemNames = itemNames;
        this.itemValues = itemValues;
        this.selectedItem = selectedItem;
        this.onSelectCallback = onSelectCallback;
    }

    public List<String> getItemNames() {
        return itemNames;
    }

    public List<String> getItemValues() {
        return itemValues;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public OnSelectCallback getOnSelectCallback() {
        return onSelectCallback;
    }

    @Override
    public CommonItemView createView(ViewGroup parent) {
        return new NameValueSelectView(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_name_spiner_val, parent, false));
    }

    public interface OnSelectCallback {
        void onSelect(View view, String selectedValue, int position);
    }
}
package com.pax.poslink.view;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon on 2017/9/18.
 */

public class NameValueSelectView implements CommonItemView<NameValueSelectEntity> {
    private View view;
    private final TextView nameTxt;
    private SelectSpinner valueSpinner;

    public NameValueSelectView(View view) {
        this.view = view;
        nameTxt = (TextView) view.findViewById(R.id.name_txt);
        valueSpinner = (SelectSpinner) view.findViewById(R.id.value_spinner);
    }

    @Override
    public void render(final NameValueSelectEntity renderEntity) {
        nameTxt.setText(renderEntity.getName());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(valueSpinner.getContext(), android.R.layout.simple_spinner_item, renderEntity.getItemNames());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        valueSpinner.setAdapter(arrayAdapter);
        //Select item without call OnItemSelected
        valueSpinner.setSelection(renderEntity.getSelectedItem(), false);
        valueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                renderEntity.setSelectedItem(position);
                String selectedValue = renderEntity.getItemValues().get(position);
                renderEntity.setValue(selectedValue);
                if (renderEntity.getOnSelectCallback() != null) {
                    renderEntity.getOnSelectCallback().onSelect(view, selectedValue, position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public View getView() {
        return view;
    }
}

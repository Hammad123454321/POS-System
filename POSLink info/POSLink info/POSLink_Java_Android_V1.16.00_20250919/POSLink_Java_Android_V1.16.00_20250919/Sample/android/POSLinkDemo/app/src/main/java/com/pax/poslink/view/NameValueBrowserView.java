package com.pax.poslink.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon.F on 2018/1/12.
 */

public class NameValueBrowserView implements CommonItemView<NameValueBrowserEntity> {

    private View view;
    private final TextView nameTxt;
    private final TextView valueText;
    private final Button browser;

    public NameValueBrowserView(View view) {
        this.view = view;
        nameTxt = (TextView) view.findViewById(R.id.name_txt);
        valueText = (TextView) view.findViewById(R.id.string_val_txt);
        browser = (Button) view.findViewById(R.id.request_browser);
    }

    @Override
    public void render(final NameValueBrowserEntity renderEntity) {
        nameTxt.setText(renderEntity.getName());
        final int inputType = renderEntity.getInputType();
        setValueToTxt(renderEntity, inputType);
        browser.setText(renderEntity.getButtonName());
        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderEntity.getClickCallback().onClick(v, renderEntity);
            }
        });
    }

    private void setValueToTxt(final NameValueStringEntity renderEntity, int inputType) {
        valueText.setInputType(inputType);
        valueText.setText(renderEntity.getValue());
        valueText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                renderEntity.setValue(s.toString());
            }
        });
    }

    @Override
    public View getView() {
        return view;
    }
}

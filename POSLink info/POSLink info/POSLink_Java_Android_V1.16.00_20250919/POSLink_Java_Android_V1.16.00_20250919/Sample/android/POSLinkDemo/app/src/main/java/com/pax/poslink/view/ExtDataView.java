package com.pax.poslink.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;


/**
 * Created by Leon on 2017/9/5.
 */

public class ExtDataView implements CommonItemView<NameValueStringEntity> {

    private View view;
    private final TextView nameTxt;
    private final TextView valueText;

    public ExtDataView(View view) {
        this.view = view;
        nameTxt = (TextView) view.findViewById(R.id.button_extdata);
        valueText = (TextView) view.findViewById(R.id.edtext_extData);

    }

    @Override
    public void render(final NameValueStringEntity renderEntity) {
        nameTxt.setText(renderEntity.getName());
        final int inputType = renderEntity.getInputType();
        setValueToTxt(renderEntity, inputType);
        nameTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderEntity.getClickCallback().onClick(v, renderEntity);
            }
        });
    }

    private void setValueToTxt(final NameValueStringEntity renderEntity, int inputType) {
        valueText.setText(renderEntity.getValue());
        valueText.setInputType(inputType);
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

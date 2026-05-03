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

public class NameValueItemStringView implements CommonItemView<NameValueStringEntity> {

    private View view;
    private final TextView nameTxt;
    private final TextView valueText;

    public NameValueItemStringView(View view) {
        this.view = view;
        nameTxt = (TextView) view.findViewById(R.id.name_txt);
        valueText = (TextView) view.findViewById(R.id.string_val_txt);

    }

    @Override
    public void render(final NameValueStringEntity renderEntity) {
        nameTxt.setText(renderEntity.getName());
        final int inputType = renderEntity.getInputType();
        setValueToTxt(renderEntity, inputType);
    }

    private void setValueToTxt(final NameValueStringEntity renderEntity, int inputType) {
        valueText.setText(renderEntity.getValue());
        valueText.setInputType(inputType);
        valueText.setHint(renderEntity.getHint());
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
        valueText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (renderEntity.getClickCallback() != null)
                    renderEntity.getClickCallback().onClick(v, renderEntity);
            }
        });
    }

    @Override
    public View getView() {
        return view;
    }
}

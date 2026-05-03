package com.pax.poslink.view;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon.F on 2018/1/23.
 */

public class TitleItemView implements CommonItemView<TitleItemEntity> {

    private final TextView textView;
    private View view;

    public TitleItemView(View view) {
        this.view = view;
        textView = (TextView) view.findViewById(android.R.id.text1);
    }

    @Override
    public void render(TitleItemEntity renderEntity) {
        textView.setTextColor(Color.BLACK);
        textView.setText(renderEntity.getName());
    }

    @Override
    public View getView() {
        return view;
    }
}

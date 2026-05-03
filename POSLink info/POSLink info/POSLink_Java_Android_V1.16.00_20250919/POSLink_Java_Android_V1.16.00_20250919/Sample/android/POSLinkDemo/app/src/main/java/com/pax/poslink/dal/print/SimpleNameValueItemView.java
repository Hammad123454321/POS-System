package com.pax.poslink.dal.print;

import android.view.View;
import android.widget.TextView;

import com.pax.poslink.R;


/**
 * Created by Leon on 2017/12/4.
 */

public class SimpleNameValueItemView {


    private View rootView;
    private final TextView nameTxt;
    private final TextView valTxt;

    public SimpleNameValueItemView(View rootView) {
        this.rootView = rootView;
        nameTxt = (TextView) rootView.findViewById(R.id.name_value_name);
        valTxt = (TextView) rootView.findViewById(R.id.name_value_val);
    }

    public void render(SimpleNameValueEntity entity) {
        nameTxt.setText(entity.getName());
        valTxt.setText(entity.getValue());
    }

    public View getView() {
        return rootView;
    }
}

package com.pax.poslink.dal.print;

import android.view.View;
import android.widget.TextView;

import com.pax.poslink.R;

import java.text.DecimalFormat;

/**
 * Created by Leon on 2017/12/13.
 */

public class OrderItemView {

    private View rootView;

    private TextView nameTxt, numberTxt, priceTxt;

    public OrderItemView(View rootView) {
        this.rootView = rootView;
        nameTxt = (TextView) rootView.findViewById(R.id.tv_name);
        numberTxt = (TextView) rootView.findViewById(R.id.tv_number);
        priceTxt = (TextView) rootView.findViewById(R.id.tv_price);
    }

    public void render(OrderItem entity) {
        nameTxt.setText(entity.getName());
        double price;
        if (entity.getNumber() > 0) {
            numberTxt.setText(String.format("X %d", entity.getNumber()));
            price = entity.getNumber() * entity.getPrice();
        } else {
            numberTxt.setText("");
            price = entity.getPrice();
        }
        DecimalFormat df = new DecimalFormat("0.00");
        priceTxt.setText(String.format("$ %s", df.format(price)));
    }

    public View getView() {
        return rootView;
    }
}

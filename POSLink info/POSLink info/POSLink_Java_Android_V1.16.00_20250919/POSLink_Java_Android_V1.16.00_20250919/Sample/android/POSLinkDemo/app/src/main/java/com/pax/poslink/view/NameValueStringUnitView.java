package com.pax.poslink.view;

import android.view.View;
import android.widget.TextView;

import com.pax.poslink.R;

/**
 * Created by Leon.F on 2018/6/8.
 */
public class NameValueStringUnitView extends NameValueItemStringView {

    private final TextView unitText;

    public NameValueStringUnitView(View view) {
        super(view);
        unitText = (TextView) view.findViewById(R.id.txt_unit);
    }

    @Override
    public void render(NameValueStringEntity renderEntity) {
        super.render(renderEntity);
        if (renderEntity instanceof NameStringWithUnitEntity) {
            unitText.setText(((NameStringWithUnitEntity) renderEntity).getUnitStr());
        }
    }
}

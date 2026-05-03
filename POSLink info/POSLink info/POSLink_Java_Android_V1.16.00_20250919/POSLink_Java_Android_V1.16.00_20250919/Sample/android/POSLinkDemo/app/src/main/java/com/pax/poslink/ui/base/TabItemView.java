package com.pax.poslink.ui.base;

import android.view.View;
import android.widget.TextView;

import com.pax.poslink.R;
import com.pax.poslink.util.adapter.CommonItemView;

/**
 * Created by Leon on 2017/6/21.
 */

public class TabItemView implements CommonItemView<TabItemEntity>{


    private final View slideView;
    private View view;
    private final TextView tabTxt;

    public TabItemView(View view) {
        this.view = view;
        tabTxt = (TextView) view.findViewById(R.id.item_tab_txt);
        slideView = view.findViewById(R.id.item_tab_slide);
    }

    @Override
    public void render(final TabItemEntity renderEntity) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderEntity.getOnClickListener().onClick(v, renderEntity);
            }
        });
        tabTxt.setText(renderEntity.getTitle());
        slideView.setVisibility(renderEntity.isSelected()? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public View getView() {
        return view;
    }
}

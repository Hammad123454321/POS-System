package com.pax.poslink.ui.base;

import android.database.DataSetObserver;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by Leon on 2017/6/21.
 */

public class TabContainer {

    private ViewGroup rootView;

    public TabContainer(ViewGroup rootView) {
        this.rootView = rootView;
    }

    public void setAdapter(final BaseAdapter adapter) {
        rootView.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            rootView.addView(adapter.getView(i, null, rootView));
        }
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                for (int i = 0; i < adapter.getCount(); i++) {
                    adapter.getView(i, rootView.getChildAt(i), rootView);
                }
            }
        });
    }


}

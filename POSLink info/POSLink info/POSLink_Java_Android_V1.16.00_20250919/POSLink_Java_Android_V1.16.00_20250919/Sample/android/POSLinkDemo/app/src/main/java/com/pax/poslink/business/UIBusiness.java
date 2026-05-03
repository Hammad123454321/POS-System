package com.pax.poslink.business;

import android.view.ViewGroup;

import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;

import java.util.List;

/**
 * Created by Leon.F on 2018/1/19.
 */

public class UIBusiness {
    public static void notifyDataSetChangeForContainer(List<RenderEntity> entityList, ViewGroup container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            ((CommonItemView) container.getChildAt(i).getTag()).render(entityList.get(i));
        }
    }
}

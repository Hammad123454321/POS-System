package com.pax.poslink.util.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew on 2016/5/31.
 */
public class CommonAdapterInternal {
    private Map<Class, Integer> classMapViewType = new HashMap<>(10);

    public CommonAdapterInternal(Class<? extends RenderEntity>[] classArr) {
        for (int i = 0; classArr != null && i < classArr.length; i++) {
            classMapViewType.put(classArr[i], i);
        }
    }

    public int getItemViewType(Class entityCls) {
        Integer viewType = classMapViewType.get(entityCls);
        return viewType == null ? 0 : viewType;
    }

    public int getViewTypeCount() {
        return classMapViewType.size() == 0 ? 1 : classMapViewType.size();
    }
}

package com.pax.poslink.util.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class CommonBaseAdapter<T extends RenderEntity> extends BaseAdapter {

    protected final List<T> entityList;
    private CommonAdapterInternal commonAdapterInternal;

    public CommonBaseAdapter(List<T> entityList) {
        this(entityList, null);
    }

    public CommonBaseAdapter(List<T> entityList, Class<? extends RenderEntity>[] classArr) {
        this.entityList = entityList;
        commonAdapterInternal = new CommonAdapterInternal(classArr);
    }

    @Override
    public int getCount() {
        return entityList.size();
    }

    @Override
    public Object getItem(int position) {
        return entityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return commonAdapterInternal.getItemViewType(entityList.get(position).getClass());
    }

    @Override
    public int getViewTypeCount() {
        return commonAdapterInternal.getViewTypeCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonItemView itemView;
        RenderEntity renderEntity = entityList.get(position);
        if (convertView == null) {
            itemView = renderEntity.createView(parent);
            convertView = itemView.getView();
            convertView.setTag(itemView);
        } else {
            itemView = (CommonItemView) convertView.getTag();
        }
        itemView.render(renderEntity);
        return convertView;
    }
}
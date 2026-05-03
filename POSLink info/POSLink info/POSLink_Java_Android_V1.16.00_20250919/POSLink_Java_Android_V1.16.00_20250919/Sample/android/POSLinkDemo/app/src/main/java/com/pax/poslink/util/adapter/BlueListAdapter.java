package com.pax.poslink.util.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pax.poslink.R;

import java.util.List;

public class BlueListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<BlueItem> mDatas;

    public BlueListAdapter(Context context, List<BlueItem> datas) {
        mInflater = LayoutInflater.from(context);
        mDatas = datas;
    }
    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_bluelist, parent, false);

            holder = new ViewHolder();

            holder.name = (TextView)convertView.findViewById(R.id.device_name);
            holder.mac = (TextView)convertView.findViewById(R.id.device_mac);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        BlueItem item = mDatas.get(position);
        holder.name.setText(item.getName());
        holder.mac.setText(item.getMac());

        return convertView;
    }

    private class ViewHolder {
        TextView name;
        TextView mac;
    }

    public static class BlueItem {
        private String name;
        private String mac;

        public BlueItem(String name, String mac) {
            this.name = name;
            this.mac = mac;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }
    }
}

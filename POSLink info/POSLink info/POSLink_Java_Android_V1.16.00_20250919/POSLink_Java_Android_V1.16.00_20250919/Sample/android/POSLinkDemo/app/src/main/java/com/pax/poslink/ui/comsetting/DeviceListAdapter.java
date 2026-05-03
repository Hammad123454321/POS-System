package com.pax.poslink.ui.comsetting;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pax.poslink.R;

import java.util.List;

/**
 * Created by Justin.Z on 2020-1-16
 */
public class DeviceListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<UsbDevice> mDatas;

    public DeviceListAdapter(Context context, List<UsbDevice> devices) {
        mInflater = LayoutInflater.from(context);
        mDatas = devices;
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
            convertView = mInflater.inflate(R.layout.item_devicelist, parent, false);

            holder = new ViewHolder();

            holder.name = (TextView)convertView.findViewById(R.id.device_name);
            holder.mac = (TextView)convertView.findViewById(R.id.device_mac);
            holder.mac_name = convertView.findViewById(R.id.device_device_name);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        UsbDevice item = mDatas.get(position);
        holder.name.setText(item.getDeviceName());
        holder.mac.setText(item.getManufacturerName());
        holder.mac_name.setText(item.getProductName());

        return convertView;
    }

    private class ViewHolder {
        TextView name;
        TextView mac;
        TextView mac_name;
    }
}

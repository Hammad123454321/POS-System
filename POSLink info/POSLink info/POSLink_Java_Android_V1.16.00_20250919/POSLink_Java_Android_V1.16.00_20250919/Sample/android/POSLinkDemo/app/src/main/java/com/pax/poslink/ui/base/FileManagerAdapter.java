package com.pax.poslink.ui.base;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.poslink.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by linhb on 2015-10-12.
 */
public class FileManagerAdapter extends BaseAdapter {
    private Context mContext;
    private int mScreenHeight;
    private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//    private List<Integer> selectIndex = new ArrayList<>();
    private Set<Integer> selectIndex = new HashSet<>();

    public FileManagerAdapter(Context context, int screenHeight) {
        super();
        mContext = context;
        mScreenHeight = screenHeight;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg2) {
        FileMangerHolder holder;

        if(null == convertView){
            holder = new FileMangerHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.file_item, null);

            LinearLayout ll = (LinearLayout)convertView.findViewById(R.id.for_adjust_listview_item_height);

            LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) ll.getLayoutParams();
            linearParams.height = mScreenHeight;
            ll.setLayoutParams(linearParams);

            holder.icon = (ImageView) convertView.findViewById(R.id.file_item_icon);
            holder.name = (TextView) convertView.findViewById(R.id.file_item_name);
            holder.name.setTextSize(20);
            holder.name.setGravity(Gravity.CENTER|Gravity.LEFT);

            convertView.setTag(holder);
        }else{
            holder = (FileMangerHolder) convertView.getTag();
        }

        holder.icon.setImageResource((Integer)list.get(position).get("icon"));
        holder.name.setText((String)(list.get(position).get("name")));
        if (selectIndex.contains(position)) {
            holder.name.setTextColor(Color.YELLOW);
        } else {
            holder.name.setTextColor(Color.WHITE);
        }

        return convertView;
    }

    public void addSelectIndex(int index) {
        selectIndex.add(index);
    }

    public void removeSelectIndex(int index) {
        if (selectIndex.contains(index)) {
            selectIndex.remove(index);
        }
    }

    public static class FileMangerHolder{
        public ImageView icon;
        public TextView name;
    }

    public void setFileListInfo(List<Map<String, Object>> infos){
        list.clear();
        list.addAll(infos);
        notifyDataSetChanged();
    }

}

package com.pax.poslink.ui.logsetting;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.pax.poslink.BuildConfig;
import com.pax.poslink.ui.base.FileManagerAdapter;
import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.UIUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linhb on 2015-10-12.
 */
public class LogsViewerActivity extends BaseActivity {
    private TextView mCurrentPath;
    private ListView mList;
    private View mPathLine;
    private FileManagerAdapter adapter;
    private ArrayList<Map<String, Object>> infos = null;
    private List<String> selectList = new ArrayList<>();
    private boolean selectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logs_list);

        Bundle bundle = getIntent().getExtras();
        String path = bundle.getString("LogsPath");
        String mode = bundle.getString("SelectMode");
        if ("1".equals(mode)) {
            selectMode = true;
        }

        initView(path);
    }

    private void initView(String initPath) {
        mCurrentPath = (TextView) findViewById(R.id.logs_path);

        if (selectMode) {
            RelativeLayout layout = findViewById(R.id.select_view);
            layout.setVisibility(View.VISIBLE);
            findViewById(R.id.file_return).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            findViewById(R.id.file_select).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("FilePaths", selectList.toArray(new String[selectList.size()]));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
        mPathLine = findViewById(R.id.file_path_line);
        mList = (ListView) findViewById(R.id.logs_list);
        mList.setOnItemClickListener(selectMode ? selectClickListener : clickListener);

        initList(initPath);
    }

    private void initList(String path) {
        File file = new File(path);
        File[] fileList = file.listFiles();
        infos = new ArrayList<Map<String, Object>>();
        Map<String, Object> item;

        mCurrentPath.setText(file.getPath());
        mCurrentPath.setVisibility(View.VISIBLE);
        mPathLine.setVisibility(View.VISIBLE);

        try {
            for (File i : fileList) {
                item = new HashMap<String, Object>();
                if (!i.getName().startsWith(".") && i.getName().endsWith(".log")) {
                    item.put("icon", R.drawable.file);
                    item.put("name", i.getName());
                    item.put("path", i.getAbsolutePath());
                    infos.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!infos.isEmpty()) {
            Collections.sort(infos, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> object1, Map<String, Object> object2) {
                    return ((String) object1.get("name")).toLowerCase().compareTo(((String) object2.get("name")).toLowerCase());
                }
            });
        }

        adapter = new FileManagerAdapter(this, this.getWindowManager().getDefaultDisplay().getHeight() / 10);
        adapter.setFileListInfo(infos);
        mList.setAdapter(adapter);
    }

    public void notifyPositionChange(int position, boolean active) {

        int visibleFirstPosi = mList.getFirstVisiblePosition();
        int visibleLastPosi = mList.getLastVisiblePosition();
        if (position >= visibleFirstPosi && position <= visibleLastPosi) {
            View view = mList.getChildAt(position - visibleFirstPosi);
            FileManagerAdapter.FileMangerHolder holder = (FileManagerAdapter.FileMangerHolder) view.getTag();
            ((FileManagerAdapter.FileMangerHolder) view.getTag()).name.setTextColor(active ? Color.YELLOW : Color.WHITE);
        } else {

        }

//        View view = mList.getChildAt(position);
//        if (view == null) {
//            return;
//        }
//        FileManagerAdapter.FileMangerHolder fileMangerHolder = ((FileManagerAdapter.FileMangerHolder) view.getTag());
//        ((FileManagerAdapter.FileMangerHolder) view.getTag()).name.setTextColor(active ? Color.YELLOW : Color.WHITE);

    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(v.getContext(), BuildConfig.APPLICATION_ID + ".fileprovider", new File((String) (infos.get(position).get("path"))));
                intent.setDataAndType(contentUri, "text/plain");
            } else {
                Uri uri = Uri.fromFile(new File((String) (infos.get(position).get("path"))));
                intent.setDataAndType(uri, "text/plain");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                UIUtil.showToast(getApplicationContext(), "No application can open this file!", Toast.LENGTH_SHORT);
            }
        }
    };

    private AdapterView.OnItemClickListener selectClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
            String path = (String) infos.get(position).get("path");
            if (!selectList.contains(path)) {
//                notifyPositionChange(position, true);
                adapter.addSelectIndex(position);
                adapter.notifyDataSetChanged();
                selectList.add(path);
            } else {
//                notifyPositionChange(position, false);
                adapter.removeSelectIndex(position);
                adapter.notifyDataSetChanged();
                selectList.remove(path);
            }
//            File file = new File((String) (infos.get(position).get("path")));
        }
    };

}

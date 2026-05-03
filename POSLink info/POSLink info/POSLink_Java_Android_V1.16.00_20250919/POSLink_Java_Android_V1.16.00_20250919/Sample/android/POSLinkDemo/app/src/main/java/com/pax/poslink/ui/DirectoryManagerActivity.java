package com.pax.poslink.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.ui.base.FileManagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by linhb on 2015-10-12.
 */
public class DirectoryManagerActivity extends BaseActivity implements OnClickListener {
    private TextView mCurrentPath;
    private Button mReturn;
    private Button mOK;
    private String mReturnPath;
    private ListView mList;
    private View mPathLine;
    private FileManagerAdapter adapter;
    private ArrayList<Map<String, Object>> infos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_list);

        Bundle bundle=getIntent().getExtras();
        String path=bundle.getString("SaveImagePath");

        initView(path);
    }

    private void initView(String initPath) {
        mCurrentPath = (TextView) findViewById(R.id.file_path);
        mReturn = (Button) findViewById(R.id.file_return);
        mReturn.setOnClickListener(this);
        mReturn.setWidth(this.getWindowManager().getDefaultDisplay().getWidth() / 5);
        mReturn.setTextSize(20);
        mReturn.setGravity(Gravity.CENTER);

        mOK = (Button) findViewById(R.id.file_select);
        mOK.setOnClickListener(this);
        mOK.setWidth(this.getWindowManager().getDefaultDisplay().getWidth() / 5);
        mOK.setTextSize(20);
        mOK.setGravity(Gravity.CENTER);

        mPathLine = findViewById(R.id.file_path_line);
        mList = (ListView) findViewById(R.id.file_list);
        mList.setOnItemClickListener(clickListener);

        initList(initPath);
    }

    private void initList(String path) {
        File file = new File(path);
        File[] fileList = file.listFiles();
        infos = new ArrayList<Map<String, Object>>();
        Map<String, Object> item;
        if (path.equals(Environment.getExternalStorageDirectory().toString())) {
            mCurrentPath.setText(Environment.getExternalStorageDirectory().toString());
            mReturn.setVisibility(View.GONE);
            mPathLine.setVisibility(View.GONE);
        } else {
            mReturn.setText("Back");
            mReturn.setVisibility(View.VISIBLE);
            mReturnPath = file.getParent();
            mCurrentPath.setText(file.getPath());
            mCurrentPath.setVisibility(View.VISIBLE);
            mPathLine.setVisibility(View.VISIBLE);
        }

        try {
            for (File i:fileList) {
                item = new HashMap<String, Object>();
                if(!i.getName().startsWith(".")) {
                    if (i.isDirectory()){
                        item.put("icon", R.drawable.directory);
                        item.put("name", i.getName());
                        item.put("path", i.getAbsolutePath());
                        infos.add(item);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!infos.isEmpty()){
            Collections.sort(infos, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> object1, Map<String, Object> object2) {
                    return ((String)object1.get("name")).toLowerCase().compareTo(((String)object2.get("name")).toLowerCase());
                }
            });
        }

        adapter = new FileManagerAdapter(this, this.getWindowManager().getDefaultDisplay().getHeight() / 10);
        adapter.setFileListInfo(infos);
        mList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.file_return:
                if (mReturnPath.length() > 0) {
                    initList(mReturnPath);
                }
                break;
            case R.id.file_select:
                Intent intent = new Intent();
                intent.putExtra("SaveImagePath", mCurrentPath.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    private AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
        String nextPath = (String) (infos.get(position).get("path"));
        initList(nextPath);
        }
    };

}

package com.pax.poslink.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.adapter.RenderEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin.Z on 2020-5-25
 */
public abstract class BaseDataActivity<T> extends BaseActivity {

    private String msg = "";
    protected List<RenderEntity> renderEntityList = new ArrayList<>();
    protected ViewGroup container;
    protected Gson gson = new Gson();
    protected T object;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_commercial_activity);
        Bundle bundle = getIntent().getExtras();
        msg = bundle.getString(getMsgKey());
        extDataGet(bundle);
        initView();
        initList();
    }

    protected abstract String getMsgKey();

    protected abstract T getObject();

    protected void extDataGet(Bundle bundle) {

    }

    protected void extDataSave(Intent intent) {

    }

    protected void dataClear() {

    }

    private void initView() {
        object = TextUtils.isEmpty(msg) ? getObject() : (T) gson.fromJson(msg, getObject().getClass());
        container = findViewById(R.id.commercial_container);
        Button backBtn = (Button) findViewById(R.id.payment_extData_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button clearBtn = findViewById(R.id.payment_extData_clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataClear();
                object = getObject();
                initList();
            }
        });

        Button saveBtn = (Button) findViewById(R.id.payment_extData_save);
        saveBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onClickSave();
                getData();
                Intent intent = new Intent();
                String msg = gson.toJson(object);
                intent.putExtra(getMsgKey(), msg);
                extDataSave(intent);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    protected void initList() {

    }

    protected abstract void getData();
//    abstract void onClickClear();
}

package com.pax.poslink.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pax.poslink.R;
import com.pax.poslink.util.adapter.RenderEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin.Z on 2021-3-2
 */
public abstract class BaseListDataActivity<T> extends BaseActivity implements View.OnClickListener {

    private Button btn_add, btn_delete, btn_cancel, btn_save;
    private TextView displayData;
    protected ViewGroup container;

    protected List<RenderEntity> renderEntityList = new ArrayList<>();
    private StringBuilder stringBuilder;
    protected Gson gson;
    private String dataJson = "";

    private List<T> dataList = new ArrayList<>();
    private T data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_list_data);
        stringBuilder = new StringBuilder();
        Bundle bundle = getIntent().getExtras();
        gson = new Gson();
        String msg = bundle.getString(getDisplayKey());
        dataJson = bundle.getString(getMsgKey());
        if (!TextUtils.isEmpty(dataJson))
            dataList = gson.fromJson(dataJson, new TypeToken<List<T>>(){}.getType());
        stringBuilder.append(TextUtils.isEmpty(msg) ? "" : msg);
        initView();
    }

    private void initView() {
        data = getObject();
        displayData = findViewById(R.id.tv_display_data);
        displayData.setText(stringBuilder.toString());

        container = findViewById(R.id.data_container);
        btn_save = findViewById(R.id.btn_save);
        btn_add = findViewById(R.id.btn_add);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_delete = findViewById(R.id.btn_delete);
        btn_save.setOnClickListener(this);
        btn_add.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

        initList();
    }

    protected abstract String getMsgKey();

    protected abstract String getDisplayKey();

    protected abstract T getObject();

    protected abstract T getData();

    protected abstract String formatData(T t);

    @CallSuper
    protected void initList() {
        renderEntityList.clear();
        container.removeAllViews();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                data = getData();
                if (checkFieldEmpty(data))
                    return;
                dataList.add(data);
                if (stringBuilder.length() != 0)
                    stringBuilder.append("|");
                stringBuilder.append(formatData(data));
                displayData.setText(stringBuilder.toString());
                break;
            case R.id.btn_save:
                Gson gson = new Gson();
                String lineItemDetails = gson.toJson(dataList);
                Intent intent = new Intent();
                intent.putExtra(getMsgKey(), lineItemDetails);
                intent.putExtra(getDisplayKey(), stringBuilder.toString());
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.btn_cancel:
                finish();
                break;
            case R.id.btn_delete:
                if (stringBuilder.length() < 1)
                    return;
                dataList.remove(dataList.size() - 1);
                int index = stringBuilder.lastIndexOf("|");
                if (index < 0) {
                    stringBuilder = new StringBuilder();
                } else {
                    stringBuilder.delete(index, stringBuilder.length());
                }
                displayData.setText(stringBuilder.toString());
                break;
            default:
                break;
        }
    }

    public boolean checkFieldEmpty(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();

        for (Field field : fields) {
            String type = field.getGenericType().toString();
            if ("class java.lang.String".equals(type)) {
                String value = null;
                try {
                    value = (String) field.get(object);
                } catch (IllegalAccessException e) {
                    return false;
                }
                if (!checkIsEmpty(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkIsEmpty(String value) {
        if (value == null || value.length() < 1) {
            return true;
        }
        return false;
    }
}

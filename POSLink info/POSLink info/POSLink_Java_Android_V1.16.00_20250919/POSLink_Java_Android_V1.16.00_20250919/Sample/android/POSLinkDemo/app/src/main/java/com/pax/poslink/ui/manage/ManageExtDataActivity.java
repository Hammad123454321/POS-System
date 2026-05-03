package com.pax.poslink.ui.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.UIUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Justin.Z on 2021-5-20
 */
public class ManageExtDataActivity extends BaseActivity implements View.OnClickListener {

    private final static String tags[] = {
            "ReportStatus"
    };

    private Map<String, EditText> m_extData = new HashMap<String, EditText>();
    private String extData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            Bundle bundle = getIntent().getExtras();
            extData = bundle.getString("extdata");
        }
        else {
            extData = savedInstanceState.getString("extdata" + this.getClass().getName());
        }
        setContentView(R.layout.activity_manage_extdata);
        findViewById(R.id.extData_back).setOnClickListener(this);
        findViewById(R.id.extData_save).setOnClickListener(this);
        initJob();
        unpackExtData(extData);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String data = packExtData();
        outState.putString("extdata" + this.getClass().getName(), data);
    }

    private void initJob() {
        int count = 0;
        m_extData.put(tags[count++], (EditText) findViewById(R.id.payment_ext_reportStatus));
    }

    private String packExtData()
    {
        String extData = "";
        Set<Map.Entry<String, EditText>> allSet=m_extData.entrySet();
        Iterator<Map.Entry<String, EditText>> iter=allSet.iterator();
        while(iter.hasNext()){
            Map.Entry<String, EditText> item=iter.next();
            if(item.getValue().getText().toString().length() > 0)
                extData += "<" + item.getKey() + ">" + item.getValue().getText().toString() + "</" + item.getKey() + ">";
        }
        return extData;
    }

    private void unpackExtData(String data)
    {
        int i;
        for(i = 0; i < tags.length; ++i) {
            EditText tmp = m_extData.get(tags[i]);
            tmp.setText(UIUtil.findXMl(data, tags[i]));
            m_extData.put(tags[i], tmp);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.extData_back:
                finish();
                break;
            case R.id.extData_save:
                String result = packExtData();
                Intent intent = new Intent();
                intent.putExtra("extdata", result);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }
}

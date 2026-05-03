package com.pax.poslink.ui.vas;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class VASGoogleCapActivity extends BaseActivity {

    private LinearLayout systemLayout, uiLayout, checkoutLayout, cvmLayout;

    private String vasCap = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_vas_cap);
        vasCap = getIntent().getStringExtra("vasCap");

        systemLayout = findViewById(R.id.layout_system);
        uiLayout = findViewById(R.id.layout_ui);
        checkoutLayout = findViewById(R.id.layout_checkout);
        cvmLayout = findViewById(R.id.layout_cvm);

        Button backBtn = findViewById(R.id.vas_special_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button saveBtn = findViewById(R.id.vas_special_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                String str = saveData();
                intent.putExtra("vas_cap", str);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        initData();
    }

    private void initData() {
        if (TextUtils.isEmpty(vasCap)) {
            return;
        }

        List<LinearLayout> linearLayoutList = new ArrayList<LinearLayout>() {
            {
                add(systemLayout);
                add(uiLayout);
                add(checkoutLayout);
                add(cvmLayout);
            }
        };
        for (int j = 0; j < linearLayoutList.size(); j++) {
            String str = vasCap.substring(j * 8, (j + 1) * 8);
            LinearLayout linearLayout = linearLayoutList.get(j);
            int a = linearLayout.getChildCount() - 1;
            for (int i = 0; i < a; i++) {
                View view = linearLayout.getChildAt(i + 1);
                if (view instanceof CheckBox) {
                    if (str.charAt(i) == '1') {
                        ((CheckBox) view).setChecked(true);
                    }
                }
            }
        }
    }

    private String saveData() {
        byte[] result = new byte[4];
        byte b;
        byte[] s = new byte[]{0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80};
        List<LinearLayout> linearLayoutList = new ArrayList<LinearLayout>() {
            {
                add(systemLayout);
                add(uiLayout);
                add(checkoutLayout);
                add(cvmLayout);
            }
        };
        for (int j = 0; j < 4; j++) {
            b = 0x00;
            LinearLayout linearLayout = linearLayoutList.get(j);
            int a = linearLayout.getChildCount() - 1;
            for (int i = 0; i < a; i++) {
                View view = linearLayout.getChildAt(i + 1);
                if (view instanceof CheckBox) {
                    if (((CheckBox) view).isChecked()) {
                        b |= s[i];
                    }
                }
            }
            result[j] = b;
        }
        return StringUtil.bcdToBinaryStr(StringUtil.reverse(result));
    }
}

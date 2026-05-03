package com.pax.poslink.dal.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.peripheries.POSLinkScanner;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.view.NameValueSelectEntity;

import java.util.Arrays;

public class ScanResultModeActivity extends BaseActivity {
    private static final String BARCODE = "BARCODE";
    private static final String CODE_FORMAT = "CODE_FORMAT";
    private EditText etCode;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            String barcode = intent.getStringExtra(BARCODE);
            String format = intent.getStringExtra(CODE_FORMAT);
            etCode.setText(barcode);
            UIUtil.showToast(getApplicationContext(), "BroadcastReceiver barcode: " + barcode + "  format:" + format, Toast.LENGTH_LONG);
        }
    };

    public static void start(Context context) {
        Intent starter = new Intent(context, ScanResultModeActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result_mode);
        initView();
        init();
    }

    private void initView() {
        etCode = findViewById(R.id.edit_code);
    }

    private void init() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.BROADCAST_SCAN_RESULT_MODE);
        registerReceiver(mReceiver, filter);
        final NameValueSelectEntity scanResultModeSelectEntity = new NameValueSelectEntity("ScanResultMode",
                "0",
                Arrays.asList("0", "1", "2"),
                Arrays.asList("0", "1", "2"), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, String selectedValue, int position) {
                etCode.setText("");
                boolean isSuccess = POSLinkScanner.setScanResultMode(ScanResultModeActivity.this, Integer.valueOf(selectedValue));
                if (isSuccess){
                    UIUtil.showToast(getApplicationContext(), "ScanResultMode:"+selectedValue, Toast.LENGTH_SHORT);
                }else {
                    UIUtil.showToast(getApplicationContext(), "Failure", Toast.LENGTH_SHORT);
                }
            }
        });
        LinearLayout settingContainer = (LinearLayout) findViewById(R.id.scan_result_mode_container);
        settingContainer.removeAllViews();
        CommonItemView itemView = scanResultModeSelectEntity.createView(settingContainer);
        itemView.render(scanResultModeSelectEntity);
        settingContainer.addView(itemView.getView());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        if (keyCode == 138){
            etCode.setText("");
        }
        return super.onKeyUp(keyCode, event);
    }
}

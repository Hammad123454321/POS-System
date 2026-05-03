package com.pax.poslink.dal.scan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;

public class ScanActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, ScanActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        getSupportFragmentManager().beginTransaction().add(R.id.scan_fragment_container, ScanFragment.newInstance()).commitAllowingStateLoss();
    }
}

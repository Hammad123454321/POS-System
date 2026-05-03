package com.pax.poslink.ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.pax.poslink.R;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.util.StatusBarCompat;

/**
 * Created by Leon on 2017/11/24.
 */

public class BaseActivity extends FragmentActivity {

    protected ActivityResultReceiver activityResultReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.compat(this, getResources().getColor(R.color.color_primary));
//        StatusBarCompat.setRootViewProperty((ViewGroup) getWindow().getDecorView());
    }

    protected void setActivityResultReceiver(ActivityResultReceiver activityResultReceiver) {
        this.activityResultReceiver = activityResultReceiver;
    }

    protected void onActivityResultReceive(String data) {
        if (activityResultReceiver != null) activityResultReceiver.onReceive(data);
    }

}

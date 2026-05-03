package com.pax.poslink.ui.base;

import android.os.Handler;
import android.os.Message;

import androidx.fragment.app.Fragment;

import com.pax.poslink.PosLinkHandler;


public abstract class BaseFragment extends Fragment {
    protected final String TAG = getClass().getSimpleName(); //loglabel;

    protected Handler mHandler = new PosLinkHandler<>(this);

    public void handleMessage(Message msg) {

    }
}

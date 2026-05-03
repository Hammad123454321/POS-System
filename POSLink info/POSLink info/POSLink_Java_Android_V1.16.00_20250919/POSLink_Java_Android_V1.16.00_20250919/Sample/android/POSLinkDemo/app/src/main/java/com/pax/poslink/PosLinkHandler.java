package com.pax.poslink;

import android.os.Handler;
import android.os.Message;

import com.pax.poslink.ui.base.BaseFragment;
import com.pax.poslink.ui.base.TaskFragment;

import java.lang.ref.WeakReference;


public class PosLinkHandler<T extends BaseFragment> extends Handler {
    private WeakReference<T> context;

    public PosLinkHandler(T fragment) {
        context = new WeakReference<>(fragment);
    }

    @Override
    public void handleMessage(Message msg) {
        T fragment = context.get();
        if (fragment != null ) {
            fragment.handleMessage(msg);
        }
    }
}

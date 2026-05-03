package com.pax.poslink.log;

import android.util.Log;

import com.pax.poslink.util.LogStaticWrapper;

/**
 * Created by Leon on 2017/4/28.
 */

public class LogStaticAndroidImpl extends LogStaticWrapper.LogImpl{

    @Override
    public void v(String msg) {
        super.v(msg);
        Log.v("DEBUG_POSLink", "Current Thread:" + Thread.currentThread().getName() + " " +  msg);
    }

    @Override
    public void d(String info) {
        super.d(info);
        Log.d("DEBUG_POSLink", "Current Thread:" + Thread.currentThread().getName() + " " +  info);
    }

    @Override
    public void e(String msg) {
        super.e(msg);
        Log.e("DEBUG_POSLink", "Current Thread:" + Thread.currentThread().getName() + " " +  msg);
    }

    @Override
    public void exceptionLog(Throwable e) {
        super.exceptionLog(e);
        Log.e("DEBUG_POSLink", "Current Thread:" + Thread.currentThread().getName() + " " +  e.getLocalizedMessage());
    }

    @Override
    public void debugExceptionPrint(Throwable throwable) {
        super.debugExceptionPrint(throwable);
        throwable.printStackTrace();
    }
}

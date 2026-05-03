package com.pax.poslink.ui.base;


import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;

import com.pax.poslink.AsyncPosLinkTask;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.base.BaseRequest;
import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.POSLinkCreatorWrapper;
import com.pax.poslink.util.thread.AppThreadPool;

public abstract class RequestResponseFragment<T extends BaseRequest, E extends BaseResponse, D extends RequestFragment> extends BaseRequestResponseFragment<T, D> {
    protected PosLink poslink;
    protected T request;
    protected ProcessTransResult ptr;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPOSLink();
        setTask();
    }

    private void initPOSLink() {
        POSLinkCreatorWrapper.createSync(getContext(), new AppThreadPool.FinishInMainThreadCallback<PosLink>() {
            @Override
            public void onFinish(PosLink result) {
                poslink = result;
            }
        });
    }

    protected abstract E getResponse();

    @Override
    public void onPreRequest(T request) {
        this.request = request;
        mTask = new AsyncPosLinkTask(this);
        dataFragment.setData(mTask);
        mTask.execute();
    }

    @Override
    public void onTaskCompleted() {
        super.onTaskCompleted();
//        request = null;
        // There will be 2 separate results that you must handle. First is the
        // ProcessTransResult, this will give you the result of the
        // request to call poslink. PaymentResponse should only be checked if
        // ProcessTransResultCode.Code == OK.
        if (ptr.Code == ProcessTransResult.ProcessTransResultCode.OK) {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_SUCCESSED;
            msg.obj = getResponse();
            mHandler.sendMessage(msg);
            Log.i(TAG, "Transaction succeed!");
        } else if (ptr.Code == ProcessTransResult.ProcessTransResultCode.TimeOut) {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_TIMEOOUT;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            mHandler.sendMessage(msg);

            Log.e(TAG, "Transaction TimeOut! " + String.valueOf(ptr.Code));
            Log.e(TAG, "Transaction TimeOut! " + ptr.Msg);
        } else {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_FAILURE;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, String.valueOf(ptr.Code));
            b.putString(Constant.DIALOG_MESSAGE, ptr.Msg);
            msg.setData(b);
            mHandler.sendMessage(msg);

            Log.e(TAG, "Transaction Error! " + String.valueOf(ptr.Code));
            Log.e(TAG, "Transaction Error! " + ptr.Msg);
        }
    }

    @Override
    public void onDestroy() {
        if (mTask != null) {
            mTask.dismissDialog();
        }
        super.onDestroy();
    }
}

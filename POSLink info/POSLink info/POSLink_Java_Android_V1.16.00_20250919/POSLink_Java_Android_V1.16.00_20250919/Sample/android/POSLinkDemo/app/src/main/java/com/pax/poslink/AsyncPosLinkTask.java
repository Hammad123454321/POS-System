package com.pax.poslink;

import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;

import com.pax.poslink.ui.base.TaskFragment;
import com.pax.poslink.util.POSLinkThreadPool;

/**
 * Created by linhb on 2015-09-22.
 *
 * History：
 *   Justin-2019-5-30: The AsyncTask thread has a low priority and causes USB traffic to receive data slowly, so create a new thread.
 */
public class AsyncPosLinkTask{
    private TaskFragment fragment;

    private boolean isCompleted;

    private Dialog mLoadingDialog;

    public AsyncPosLinkTask(TaskFragment fragment)
    {
        this.fragment = fragment;
    }

    public void execute() {
        onPreExecute();

        POSLinkThreadPool.getInstance().runInSingleThread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                doInBackground();
                Handler handler = new Handler(  Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute();
                    }
                });
            }
        });

    }

    protected void onPreExecute() {
        displayDialog();
    }


    protected Void doInBackground() {
        fragment.run();
        return null;
    }


    protected void onPostExecute() {
        isCompleted = true;
        notifyActivityTaskCompleted();
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    public void dismissDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    /**
     * @param fragment the required fragment
     */
    public void setFragment(TaskFragment fragment) {
        if (fragment == null)
        {
//            mLoadingDialog.dismiss();
            return;
        }

        this.fragment = fragment;

        if (!isCompleted)
        {
            displayDialog();
        }

        if (isCompleted)
        {
            notifyActivityTaskCompleted();
        }
    }

    private void notifyActivityTaskCompleted() {
        if (null != fragment)
        {
            fragment.onTaskCompleted();
            //cancel(true);
        }
    }

    private void displayDialog() {
        mLoadingDialog = fragment.createDialog();
        if (mLoadingDialog != null) {
            mLoadingDialog.show();
        }
    }

}

package com.pax.poslink.widget;

/**
 * Created by linhb on 2015-09-16.
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.pax.poslink.util.LogStaticWrapper;

public class ProcessProgressDialog {

    public interface OnSetListener{
        void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss);
    }

    public static Dialog createDialog(Context context, String msg, boolean cancelable, boolean enDismiss, OnSetListener onSetListener) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(msg);
        dialog.setIndeterminate(true);
        dialog.setCancelable(cancelable);
        if(!enDismiss)
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                    return (i == KeyEvent.KEYCODE_BACK && keyEvent.getRepeatCount() == 0);
                }
            });
        dialog.setCanceledOnTouchOutside(false);
        if (onSetListener != null)
            onSetListener.onSetListener(dialog, cancelable, enDismiss);
        LogStaticWrapper.getLog().d("ProcessProgressDialog, onCreateDialog end");
        return dialog;
    }
}

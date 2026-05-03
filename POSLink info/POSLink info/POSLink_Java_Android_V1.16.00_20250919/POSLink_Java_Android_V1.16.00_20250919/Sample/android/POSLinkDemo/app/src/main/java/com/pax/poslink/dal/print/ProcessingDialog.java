package com.pax.poslink.dal.print;

import android.app.ProgressDialog;

/**
 * Created by Leon on 2017/10/24.
 */

public class ProcessingDialog {

    private ProgressDialog progressDialog;

    public ProcessingDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public void start(String msg, boolean cancelable) {
        progressDialog.setMessage(msg);
        progressDialog.setCancelable(cancelable);
        progressDialog.show();
    }

    public void dismiss() {
        progressDialog.dismiss();
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }
}

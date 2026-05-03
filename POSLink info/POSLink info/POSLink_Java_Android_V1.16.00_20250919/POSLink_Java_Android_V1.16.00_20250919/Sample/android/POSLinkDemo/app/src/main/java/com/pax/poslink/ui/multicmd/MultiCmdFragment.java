package com.pax.poslink.ui.multicmd;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.pax.poslink.MultipleCommandsRequest;
import com.pax.poslink.MultipleCommandsResponse;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.base.BaseResponse;
import com.pax.poslink.ui.base.RequestResponseFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.JsonFormat;
import com.pax.poslink.util.JsonUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.widget.MsgDialog;
import com.pax.poslink.widget.ProcessProgressDialog;


public class MultiCmdFragment extends RequestResponseFragment<MultipleCommandsRequest, MultipleCommandsResponse, MultiCmdRequestFragment> implements ProcessProgressDialog.OnSetListener {

    public MultiCmdFragment() {
        // Required empty public constructor
    }

    public static MultiCmdFragment newInstance() {
        return new MultiCmdFragment();
    }

    @Override
    protected MultiCmdRequestFragment createRequestFragment() {
        return MultiCmdRequestFragment.newInstance();
    }

    @Override
    protected MultipleCommandsResponse getResponse() {
        return poslink.MultipleCommandsResponse;
    }

    @Override
    public void onPreRequest(MultipleCommandsRequest request) {
        if (!request.requests.isEmpty()) {
            if (responseLayout.getVisibility() == View.VISIBLE)
                initMultiRequestResponse();
            super.onPreRequest(request);
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "MultiRequest start trans");
        // set the folder to save the "comsetting.ini" file
        poslink.appDataFolder = getContext().getFilesDir().getAbsolutePath();
        poslink.SetCommSetting(SettingINI.getCommSettingFromFile(poslink.appDataFolder + "/" + SettingINI.FILENAME));

        poslink.MultipleCommandsRequest = request;
        ptr = poslink.ProcessTrans();
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                MultipleCommandsResponse response = (MultipleCommandsResponse)msg.obj;
                setMultiRequestResponse(response);
                break;
            case Constant.TRANSACTION_TIMEOOUT:
            case Constant.TRANSACTION_FAILURE:
                String title = msg.getData().getString(Constant.DIALOG_TITLE);
                String message =  msg.getData().getString(Constant.DIALOG_MESSAGE);
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
        }
    }

    @Override
    public Dialog createDialog() {
        return ProcessProgressDialog.createDialog(getActivity(), getResources().getString(R.string.converge_process_prompt), true, false, this);
    }

    @Override
    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
        if (cancelable) {
            dialog.setButton("Cancel Process", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    poslink.CancelTrans();
                }
            });
        }
        if (enDismiss) {
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    poslink.CancelTrans();
                }
            });
        }
    }

    private void initMultiRequestResponse() {
        setMultiRequestResponse(new MultipleCommandsResponse());
    }

    private void setMultiRequestResponse(MultipleCommandsResponse response) {
        if (responseLayout.getVisibility() == View.GONE) {
            responseLayout.setVisibility(View.VISIBLE);
        }

        responseRenderEntityList.clear();
        responseContainer.removeAllViews();

        NameValueStringEntity.ClickCallback clickCallback = new NameValueStringEntity.ClickCallback() {

            @Override
            public void onClick(View v, NameValueStringEntity entity) {
                String data = entity.getValue();
                showDialog(JsonFormat.format(data));
            }
        };

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(getString(R.string.response_resultCode), response.ResultCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(getString(R.string.response_resultTxt), response.ResultTxt));
        if (response.responses != null && !response.responses.isEmpty()) {
            for (BaseResponse item : response.responses) {
                responseRenderEntityList.add(new NameValueStringUnEditableEntity(item.getClass().getSimpleName(), JsonUtil.gsonToJson(item), clickCallback));
            }
        }

        for (RenderEntity renderEntity : responseRenderEntityList) {
            CommonItemView itemView = renderEntity.createView(responseContainer);
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
    }

    private void showDialog(String msg) {
        //Toast.makeText(getActivity(), "doubleClick", Toast.LENGTH_LONG).show();
        if (!TextUtils.isEmpty(msg)) {
            MsgDialog dialog = new MsgDialog(getActivity(), msg);
            dialog.show();
        }
    }
}

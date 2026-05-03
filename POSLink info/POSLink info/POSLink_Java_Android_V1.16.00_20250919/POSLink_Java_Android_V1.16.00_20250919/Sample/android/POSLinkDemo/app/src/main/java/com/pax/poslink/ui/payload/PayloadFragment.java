package com.pax.poslink.ui.payload;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import com.pax.poslink.PayloadRequest;
import com.pax.poslink.PayloadResponse;
import com.pax.poslink.R;
import com.pax.poslink.ReportResponse;
import com.pax.poslink.SettingINI;
import com.pax.poslink.ui.base.RequestResponseFragment;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueStringUnEditableEntity;
import com.pax.poslink.view.TitleItemEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

import java.util.HashMap;
import java.util.Map;

import static com.pax.poslink.util.Constant.TRANSACTION_FAILURE;
import static com.pax.poslink.util.Constant.TRANSACTION_SUCCESSED;
import static com.pax.poslink.util.Constant.TRANSACTION_TIMEOOUT;

/**
 * @author Justin.Z on 2021-7-15
 */
public class PayloadFragment extends RequestResponseFragment<PayloadRequest, PayloadResponse, PayloadRequestFragment> implements ProcessProgressDialog.OnSetListener {

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case TRANSACTION_SUCCESSED:
                PayloadResponse response = (PayloadResponse) msg.obj;
                setResponse(response);
                break;
            case TRANSACTION_TIMEOOUT:
            case TRANSACTION_FAILURE:
                String title = msg.getData().getString(com.pax.poslink.util.Constant.DIALOG_TITLE);
                String message = msg.getData().getString(com.pax.poslink.util.Constant.DIALOG_MESSAGE);
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
        }
    }

    public static PayloadFragment newInstance() {
        return new PayloadFragment();
    }

    @Override
    protected PayloadResponse getResponse() {
        return poslink.PayloadResponse;
    }

    @Override
    public void onPreRequest(PayloadRequest request) {
        if (responseLayout.getVisibility() == View.VISIBLE) {
            initResponse();
        }
        super.onPreRequest(request);
    }

    @Override
    public void run() {
        // set the folder to save the "comsetting.ini" file
        poslink.appDataFolder = getContext().getFilesDir().getAbsolutePath();
        poslink.SetCommSetting(SettingINI.getCommSettingFromFile(poslink.appDataFolder + "/" + SettingINI.FILENAME));

        poslink.PayloadRequest = request;
        // ProcessTrans is Blocking call, will return when the transaction is
        // complete.
        ptr = poslink.ProcessTrans();
    }

    @Override
    protected PayloadRequestFragment createRequestFragment() {
        return PayloadRequestFragment.newInstance();
    }

    private void initResponse() {
        setResponse(new PayloadResponse());
    }

    private void setResponse(PayloadResponse response) {
        if (responseLayout.getVisibility() == View.GONE) {
            responseLayout.setVisibility(View.VISIBLE);
        }

        responseRenderEntityList.clear();
        responseContainer.removeAllViews();
        Resources resources = getResources();

        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_resultCode), response.ResultCode));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.report_response_resultTxt), response.ResultTxt));
        responseRenderEntityList.add(new NameValueStringUnEditableEntity(resources.getString(R.string.payload_response_payload), response.Payload));

        CommonItemView tempEntity = null;
        Map<CommonItemView, Boolean> titleViewMap = new HashMap<>();
        for (RenderEntity renderEntity : responseRenderEntityList) {
            if (renderEntity instanceof NameValueEntity && TextUtils.isEmpty(((NameValueEntity<String>) renderEntity).getValue())) {
                continue;
            }
            CommonItemView itemView = renderEntity.createView(responseContainer);
            if (renderEntity instanceof TitleItemEntity) {
                tempEntity = itemView;
                titleViewMap.put(tempEntity, false);
            }
            if (renderEntity instanceof NameValueEntity) {
                if (tempEntity != null) {
                    titleViewMap.put(tempEntity, true);
                }
            }
            responseContainer.addView(itemView.getView());
            itemView.render(renderEntity);
        }
        for (Map.Entry<CommonItemView, Boolean> entry : titleViewMap.entrySet()) {
            if (!entry.getValue()) {
                responseContainer.removeView(entry.getKey().getView());
            }
        }
    }

    @Override
    public Dialog createDialog() {
        return ProcessProgressDialog.createDialog(getActivity(), getResources().getString(R.string.report_process_prompt), false, false, this);
    }

    @Override
    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
        //don't nee to set a listener
    }
}

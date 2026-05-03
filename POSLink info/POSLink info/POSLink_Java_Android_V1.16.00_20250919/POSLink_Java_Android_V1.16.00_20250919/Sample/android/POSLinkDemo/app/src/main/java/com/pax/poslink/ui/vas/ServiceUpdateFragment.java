package com.pax.poslink.ui.vas;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.pax.poslink.ManageRequest;
import com.pax.poslink.R;
import com.pax.poslink.ui.sendcmd.SendCmdFragment;

import java.util.ArrayList;
import java.util.List;

public class ServiceUpdateFragment extends BaseVASSmartTabFragment {

    private EditText edtUpdateID, edtUpdatePayload, edtUpdateOperation;
    private List<ManageRequest.GoogleSmartTapPushService.ServiceUpdate> serviceUpdates;
    private TextView tvResult;

    public static ServiceUpdateFragment newInstance() {
        ServiceUpdateFragment fragment = new ServiceUpdateFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service_update, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        edtUpdateID = getView().findViewById(R.id.update_id);
        edtUpdateOperation = getView().findViewById(R.id.update_operation);
        edtUpdatePayload = getView().findViewById(R.id.update_payload);
        tvResult = getView().findViewById(R.id.tv_update_data);
        if (serviceUpdates == null) serviceUpdates = new ArrayList<>();
        showResult();
    }

    public List<ManageRequest.GoogleSmartTapPushService.ServiceUpdate> getServiceUpdates() {
        return serviceUpdates;
    }

    public void setServiceUpdates (List<ManageRequest.GoogleSmartTapPushService.ServiceUpdate> serviceUpdates) {
        this.serviceUpdates = serviceUpdates;
    }

    @Override
    public String getExtData() {
        return null;
    }

    @Override
    public void addData() {
        if (checkIsEmpty()) {
            return;
        }

        ManageRequest.GoogleSmartTapPushService.ServiceUpdate serviceUpdate = new ManageRequest.GoogleSmartTapPushService.ServiceUpdate();
        serviceUpdate.UpdateID = edtUpdateID.getText().toString().trim();
        serviceUpdate.UpdateOperation = edtUpdateOperation.getText().toString().trim();
        serviceUpdate.UpdatePayload = edtUpdatePayload.getText().toString().trim();
        serviceUpdates.add(serviceUpdate);
        showResult();
    }

    @Override
    public void deleteData() {
        if (serviceUpdates.isEmpty()) {
            return;
        }

        serviceUpdates.remove(serviceUpdates.size() - 1);
        showResult();
    }

    private void showResult() {
        if (serviceUpdates.isEmpty()) {
            tvResult.setText("");
            return;
        }
        String retBuf = "";

        for (int i = 0; i < serviceUpdates.size(); i++) {
            retBuf += pack(serviceUpdates.get(i));
            if (i != serviceUpdates.size() - 1)
                retBuf += SendCmdFragment.CH_GS;
        }
        tvResult.setText(retBuf);
    }

    private String pack(ManageRequest.GoogleSmartTapPushService.ServiceUpdate serviceUpdate) {
        String result = "";
        result += serviceUpdate.UpdateID;
        result += SendCmdFragment.CH_RS;
        result += serviceUpdate.UpdateOperation;
        result += SendCmdFragment.CH_RS;
        result += serviceUpdate.UpdatePayload;
        return result;
    }

    private boolean checkIsEmpty() {
        return TextUtils.isEmpty(edtUpdateID.getText().toString())
                && TextUtils.isEmpty(edtUpdateOperation.getText().toString())
                && TextUtils.isEmpty(edtUpdatePayload.getText().toString());
    }
}
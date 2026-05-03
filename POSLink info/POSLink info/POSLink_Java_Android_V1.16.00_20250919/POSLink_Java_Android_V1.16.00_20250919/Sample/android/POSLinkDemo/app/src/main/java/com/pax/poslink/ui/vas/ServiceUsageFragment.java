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

public class ServiceUsageFragment extends BaseVASSmartTabFragment {

    private EditText serviceIDEdt, serviceStateEdt, serviceTitleEdt, serviceDesEdt;
    private List<ManageRequest.GoogleSmartTapPushService.ServiceUsage> serviceUsages;
    private TextView tvResult;

    public static ServiceUsageFragment newInstance() {
        ServiceUsageFragment fragment = new ServiceUsageFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_service_usage, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        serviceIDEdt = getView().findViewById(R.id.service_id);
        serviceStateEdt = getView().findViewById(R.id.service_state);
        serviceTitleEdt = getView().findViewById(R.id.service_title);
        serviceDesEdt = getView().findViewById(R.id.service_describe);
        tvResult = getView().findViewById(R.id.tv_usage_data);
        if (serviceUsages == null) serviceUsages = new ArrayList<>();
        showResult();
    }

    @Override
    public String getExtData() {
        String result = "";
        result += serviceIDEdt.getText().toString();
        result += SendCmdFragment.CH_RS;
        result += serviceStateEdt.getText().toString();
        result += SendCmdFragment.CH_RS;
        result += serviceTitleEdt.getText().toString();
        result += SendCmdFragment.CH_RS;
        result += serviceDesEdt.getText().toString();
        return result;
    }

    public List<ManageRequest.GoogleSmartTapPushService.ServiceUsage> getServiceUsages() {
        return serviceUsages;
    }

    @Override
    public void addData() {
        if (checkIsEmpty()) {
            return;
        }
        ManageRequest.GoogleSmartTapPushService.ServiceUsage serviceUsage = new ManageRequest.GoogleSmartTapPushService.ServiceUsage();
        serviceUsage.UsageID = serviceIDEdt.getText().toString().trim();
        serviceUsage.State = serviceStateEdt.getText().toString().trim();
        serviceUsage.Title = serviceTitleEdt.getText().toString().trim();
        serviceUsage.Describe = serviceDesEdt.getText().toString().trim();
        serviceUsages.add(serviceUsage);
        showResult();
    }

    @Override
    public void deleteData() {
        if (serviceUsages.isEmpty()) {
            return;
        }

        serviceUsages.remove(serviceUsages.size() - 1);
        showResult();
    }

    public void setServiceUsages(List<ManageRequest.GoogleSmartTapPushService.ServiceUsage> serviceUsages) {
        this.serviceUsages = serviceUsages;
    }

    private void showResult() {
        if (serviceUsages.isEmpty()) {
            tvResult.setText("");
            return;
        }
        String retBuf = "";

        for (int i = 0; i < serviceUsages.size(); i++) {
            retBuf += pack(serviceUsages.get(i));
            if (i != serviceUsages.size() - 1)
                retBuf += SendCmdFragment.CH_GS;
        }
        tvResult.setText(retBuf);
    }

    private String pack(ManageRequest.GoogleSmartTapPushService.ServiceUsage serviceUsage) {
        String result = "";
        result += serviceUsage.UsageID;
        result += SendCmdFragment.CH_RS;
        result += serviceUsage.State;
        result += SendCmdFragment.CH_RS;
        result += serviceUsage.Title;
        result += SendCmdFragment.CH_RS;
        result += serviceUsage.Describe;
        return result;
    }

    private boolean checkIsEmpty() {
        return TextUtils.isEmpty(serviceIDEdt.getText().toString()) && TextUtils.isEmpty(serviceStateEdt.getText().toString())
                && TextUtils.isEmpty(serviceTitleEdt.getText().toString()) && TextUtils.isEmpty(serviceDesEdt.getText().toString());
    }

    public String getServiceID() {
        return serviceIDEdt.getText().toString().trim();
    }

    public String getServiceState() {
        return serviceStateEdt.getText().toString().trim();
    }

    public String getServiceTitle() {
        return serviceTitleEdt.getText().toString().trim();
    }

    public String getServiceDescribe() {
        return serviceDesEdt.getText().toString().trim();
    }
}

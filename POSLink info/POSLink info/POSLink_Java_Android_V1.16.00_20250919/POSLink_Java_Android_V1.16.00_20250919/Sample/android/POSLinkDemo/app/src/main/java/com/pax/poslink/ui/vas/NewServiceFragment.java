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
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.ui.sendcmd.SendCmdFragment;

import java.util.ArrayList;
import java.util.List;

public class NewServiceFragment extends BaseVASSmartTabFragment {

    private EditText edtType, edtTitle, edtURI;
    private TextView tvResult;
    private List<ManageRequest.GoogleSmartTapPushService.NewService> newServices;

    public static NewServiceFragment newInstance() {
        NewServiceFragment fragment = new NewServiceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_service, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        edtType = getView().findViewById(R.id.new_service_type);
        edtTitle = getView().findViewById(R.id.new_service_title);
        edtURI = getView().findViewById(R.id.new_service_uri);
        tvResult = getView().findViewById(R.id.tv_new_service_data);
        if (newServices == null) newServices = new ArrayList<>();
        showResult();

    }

    public List<ManageRequest.GoogleSmartTapPushService.NewService> getNewServices() {
        return newServices;
    }

    public void setNewServices (List<ManageRequest.GoogleSmartTapPushService.NewService> newServices) {
        this.newServices = newServices;
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

        ManageRequest.GoogleSmartTapPushService.NewService newService = new ManageRequest.GoogleSmartTapPushService.NewService();
        newService.Type = edtType.getText().toString().trim();
        newService.Title = edtTitle.getText().toString().trim();
        newService.Uri = edtURI.getText().toString().trim();
        newServices.add(newService);
        showResult();

    }

    @Override
    public void deleteData() {
        if (newServices.isEmpty()) {
            return;
        }

        newServices.remove(newServices.size() - 1);
        showResult();
    }

    private void showResult() {
        if (newServices.isEmpty()) {
            tvResult.setText("");
            return;
        }
        String retBuf = "";

        for (int i = 0; i < newServices.size(); i++) {
            retBuf += pack(newServices.get(i));
            if (i != newServices.size() - 1)
                retBuf += SendCmdFragment.CH_GS;
        }
        tvResult.setText(retBuf);
    }

    private String pack(ManageRequest.GoogleSmartTapPushService.NewService newService) {
        String result = "";
        result += newService.Type;
        result += SendCmdFragment.CH_RS;
        result += newService.Title;
        result += SendCmdFragment.CH_RS;
        result += newService.Uri;
        return result;
    }

    private boolean checkIsEmpty() {
        return TextUtils.isEmpty(edtType.getText().toString()) && TextUtils.isEmpty(edtTitle.getText().toString())
                && TextUtils.isEmpty(edtURI.getText().toString());
    }
}

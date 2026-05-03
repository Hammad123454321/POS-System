package com.pax.poslink.ui.vas;


import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.gson.Gson;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;


public class VASAppleFragment extends BaseExtDataTabFragment {

    private EditText merchantIdEdt, urlModeEdt, urlEdt, keyFileMappingEdt;

    private String dataJson = "";

    public VASAppleFragment() {
        // Required empty public constructor
    }

    public static VASAppleFragment newInstance() {
        VASAppleFragment fragment = new VASAppleFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apple_vas, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        dataJson = bundle.getString("vas_specialData");

        merchantIdEdt = getView().findViewById(R.id.vas_merchant_id);
        urlModeEdt = getView().findViewById(R.id.vas_url_mode);
        urlEdt = getView().findViewById(R.id.vas_url);
        keyFileMappingEdt = getView().findViewById(R.id.vas_key_file_mapping);
        initData();
    }

    private void initData() {
        if (TextUtils.isEmpty(dataJson)) {
            return;
        }

        Gson gson = new Gson();
        ManageRequest.ApplePayVAS appleVASSpecialData;
        appleVASSpecialData = gson.fromJson(dataJson, ManageRequest.ApplePayVAS.class);
        merchantIdEdt.setText(appleVASSpecialData.MerchantID);
        urlModeEdt.setText(appleVASSpecialData.UrlMode);
        urlEdt.setText(appleVASSpecialData.Url);
        keyFileMappingEdt.setText(appleVASSpecialData.KeyFileMapping);
    }


    @Override
    public String getExtData() {
        ManageRequest.ApplePayVAS specialData = new ManageRequest.ApplePayVAS();
        specialData.MerchantID = merchantIdEdt.getText().toString().trim();
        specialData.UrlMode = urlModeEdt.getText().toString().trim();
        specialData.Url = urlEdt.getText().toString().trim();
        specialData.KeyFileMapping = keyFileMappingEdt.getText().toString().trim();
        Gson gson = new Gson();
        return gson.toJson(specialData);
    }
}

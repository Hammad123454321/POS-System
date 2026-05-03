package com.pax.poslink.ui.vas;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseExtDataTabFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.StringUtil;

import static android.app.Activity.RESULT_OK;

public class VASGoogleFragment extends BaseExtDataTabFragment {

    private Button vasCap;

    private EditText edtVasCap;
    private EditText collectIDEdt, storeLocalIDEdt, terminalIDEdt, merchantNameEdt, merchantCateEdt,
                     endTapEdt, securityEdt, oseToPPSEEdt;

    private LinearLayout serviceLayout;

    private String dataJson = "";

    public static VASGoogleFragment newInstance() {
        VASGoogleFragment fragment = new VASGoogleFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_google_vas, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        dataJson = bundle.getString("vas_specialData");

        vasCap = getView().findViewById(R.id.vas_btn_cap);
        edtVasCap = getView().findViewById(R.id.vas_google_vas_cap);
        collectIDEdt = getView().findViewById(R.id.vas_collect_id);
        storeLocalIDEdt = getView().findViewById(R.id.vas_store_local_id);
        terminalIDEdt = getView().findViewById(R.id.vas_terminal_id);
        merchantNameEdt = getView().findViewById(R.id.vas_merchant_name);
        merchantCateEdt = getView().findViewById(R.id.vas_merchant_category);
        endTapEdt = getView().findViewById(R.id.vas_endtap);
        securityEdt = getView().findViewById(R.id.vas_security);
        serviceLayout = getView().findViewById(R.id.vas_security_type);
        oseToPPSEEdt = getView().findViewById(R.id.vas_OSETOPPSE);


        vasCap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), VASGoogleCapActivity.class);
                intent.putExtra("vasCap", edtVasCap.getText().toString());
                startActivityForResult(intent, Constant.MANAGE_VAS_CAP);
            }
        });

        initData();
    }

    private void initData() {
        if (TextUtils.isEmpty(dataJson)) {
            return;
        }

        Gson gson = new Gson();
        ManageRequest.GoogleSmartTap googleVASSpecialData = new ManageRequest.GoogleSmartTap();
        googleVASSpecialData = gson.fromJson(dataJson, ManageRequest.GoogleSmartTap.class);
        edtVasCap.setText(googleVASSpecialData.GoogleSmartTapCap);
        collectIDEdt.setText(googleVASSpecialData.CollectID);
        storeLocalIDEdt.setText(googleVASSpecialData.StoreLocalID);
        terminalIDEdt.setText(googleVASSpecialData.TerminalID);
        merchantNameEdt.setText(googleVASSpecialData.MerchantName);
        merchantCateEdt.setText(googleVASSpecialData.MerchantCategory);
        endTapEdt.setText(googleVASSpecialData.EndTap);
        securityEdt.setText(googleVASSpecialData.Security);
        oseToPPSEEdt.setText(googleVASSpecialData.OseToPpse);

        String str = googleVASSpecialData.ServiceType;
        if (TextUtils.isEmpty(str)) return;
        int a = serviceLayout.getChildCount();
        for (int i = 0; i < a; i++) {
            View view = serviceLayout.getChildAt(i);
            if (view instanceof CheckBox) {
                if (str.charAt(i) == '1') {
                    ((CheckBox) view).setChecked(true);
                }
            }
        }
    }

    @Override
    public String getExtData() {
        ManageRequest.GoogleSmartTap specialData = new ManageRequest.GoogleSmartTap();


        specialData.CollectID = collectIDEdt.getText().toString().trim();
        specialData.StoreLocalID = storeLocalIDEdt.getText().toString().trim();
        specialData.TerminalID = terminalIDEdt.getText().toString().trim();
        specialData.MerchantCategory = merchantCateEdt.getText().toString().trim();
        specialData.MerchantName = merchantNameEdt.getText().toString().trim();
        specialData.EndTap = endTapEdt.getText().toString().trim();
        specialData.Security = securityEdt.getText().toString().trim();
        specialData.GoogleSmartTapCap = edtVasCap.getText().toString().trim();
        specialData.ServiceType = getServiceType();
        specialData.OseToPpse = oseToPPSEEdt.getText().toString().trim();

        Gson gson = new Gson();
        return gson.toJson(specialData);
    }

    private String getServiceType() {
        byte[] result = new byte[2];
        byte[] s = new byte[]{0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80};
        int a = serviceLayout.getChildCount();
        byte b = 0x00;
        for (int i = 0; i < a; i++) {
            if (i % 8 == 0) {
                if (b != 0x00)
                    result[1] = b;
                b = 0x00;
            }
            View view = serviceLayout.getChildAt(i);
            if (view instanceof CheckBox) {
                if (((CheckBox) view).isChecked()) {
                    int index = i % 8;
                    b |= s[index];
                }
            }
        }
        result[0] = b;
        return StringUtil.bcdToBinaryStr(result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.MANAGE_VAS_CAP:
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("vas_cap");
                    edtVasCap.setText(result);
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}

/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *
 * Module Date: 2022/07/12
 * Module Auth: Yolanda.Z
 * Description:
 *
 * Revision History:
 * Date                   Author                       Action
 * 2022/07/12            Yolanda.Z                       Create
 * ============================================================================
 */
package com.pax.poslink.ui.manage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.pax.poslink.ManageRequest;
import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;

/**
 * @author Yolanda.Z on 2022/07/12
 */
public class CustomMACInformationActivity extends BaseActivity {
    private EditText keyTypeEdt, workModeEdt, keySlotEdt, dataEdt;
    private String dataJson = "";

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_custom_mac_information);
        Button backBtn = findViewById(R.id.bt_back);
        backBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button saveBtn = findViewById(R.id.bt_save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = getExtData();
                Intent intent = new Intent();
                if (!TextUtils.isEmpty(value)) {
                    intent.putExtra("custom_mac_information", value);
                    setResult(RESULT_OK, intent);
                }
                finish();
            }
        });
        keyTypeEdt = findViewById(R.id.mac_key_type);
        workModeEdt = findViewById(R.id.mac_work_mode);
        keySlotEdt = findViewById(R.id.mac_key_slot);
        dataEdt = findViewById(R.id.mac_data);
        dataJson = getIntent().getStringExtra("custom_mac_information");

        initData();
    }

    private void initData() {
        if (TextUtils.isEmpty(dataJson)) {
            keyTypeEdt.setText("");
            workModeEdt.setText("");
            keySlotEdt.setText("");
            dataEdt.setText("");
            return;
        }
        Gson gson = new Gson();
        ManageRequest.CustomMACInformation macInformation = gson.fromJson(dataJson, ManageRequest.CustomMACInformation.class);
        keyTypeEdt.setText(macInformation.KeyType);
        workModeEdt.setText(macInformation.WorkMode);
        keySlotEdt.setText(macInformation.KeySlot);
        dataEdt.setText(macInformation.Data);
    }

    public String getExtData() {
        ManageRequest.CustomMACInformation specialData = new ManageRequest.CustomMACInformation();
        specialData.KeyType = keyTypeEdt.getText().toString().trim();
        specialData.WorkMode = workModeEdt.getText().toString().trim();
        specialData.KeySlot = keySlotEdt.getText().toString().trim();
        specialData.Data = dataEdt.getText().toString().trim();
        Gson gson = new Gson();
        return gson.toJson(specialData);
    }
}
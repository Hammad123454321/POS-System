/*
 * ============================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2018-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.

 * Module Date: 2019-5-20
 * Module Auth: Justin.Z
 * Description:

 * Revision History:
 * Date                   Author                       Action
 * 2019-5-20               Justin.Z                      Create
 * ============================================================================
 */
package com.pax.poslink.dal.icc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.entity.ApduRespInfo;
import com.pax.poslink.entity.ApduSendInfo;
import com.pax.poslink.exceptions.PaxIccException;
import com.pax.poslink.peripheries.POSLinkIcc;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.SingleButtonEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IccActivity extends BaseActivity {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup requestContainer;
    private TextView recvValTxt;

    private boolean light;
    private int slot;
    private String CMD;
    private short Lc = 0;
    private String Data;
    private short Le = 0;


    public static void start(Context context) {
        Intent starter = new Intent(context, IccActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icc);
        requestContainer = (ViewGroup) findViewById(R.id.icc_param_list_container);
        View dataTxtContainer = findViewById(R.id.serial_port_recv_data_txt);
        TextView recvNameTxt = (TextView) dataTxtContainer.findViewById(R.id.name_txt);
        recvNameTxt.setText("RecvData");
        recvValTxt = (TextView) dataTxtContainer.findViewById(R.id.string_val_txt);
        initList();

    }

    private void initList() {
        List<String> light = Arrays.asList("true", "false");
        List<Boolean> booleanList = Arrays.asList(true, false);

        renderEntityList.addAll(
                Arrays.asList(
                        new NameValueSelectEntity("light", "true", light, light),
                        new NameValueStringEntity("slot", "0", InputType.TYPE_CLASS_NUMBER, ""),
                        new NameValueStringEntity("CMD", "00a40400", InputType.TYPE_CLASS_TEXT, "CLA INS P1 P2"),
                        new NameValueStringEntity("Lc", "0", InputType.TYPE_CLASS_NUMBER, ""),
                        new NameValueStringEntity("Data", "", InputType.TYPE_CLASS_TEXT, ""),
                        new NameValueStringEntity("Le", "0", InputType.TYPE_CLASS_NUMBER, ""),
                        new SingleButtonEntity("process", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(final View v, SingleButtonEntity renderEntity) {
                                process();
                            }
                        })
                )
        );

        updateListView();

    }

    private void updateListView() {
        requestContainer.removeAllViews();
        for (RenderEntity renderEntity : renderEntityList) {
            CommonItemView itemView = renderEntity.createView(requestContainer);
            requestContainer.addView(itemView.getView());
            itemView.getView().setTag(itemView);
            itemView.render(renderEntity);
        }
    }

    private void setUpParameters() {
        for (RenderEntity entity : renderEntityList) {
            if (entity instanceof NameValueStringEntity || entity instanceof NameValueSelectEntity) {
                NameValueEntity<String> nameValueEntity = (NameValueEntity<String>) entity;
                PARAM_VALUE_SETTER_MAP.get(nameValueEntity.getName()).onSet(nameValueEntity.getValue());
            }
        }
    }

    private void process() {
        setUpParameters();
        recvValTxt.setText("");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    POSLinkIcc posLinkIcc = POSLinkIcc.getInstance(getApplicationContext());
                    posLinkIcc.light(light);
                    while (true) {
                        boolean b = posLinkIcc.detect((byte) slot);
                        if (b) {
                            byte[] res = posLinkIcc.init((byte) slot);
                            if (res == null) {
                            }
                            String re = StringUtil.bcdToStr(res);
                            setText("init response: " + re);
                            Log.d("POSLinkIccTest", "init response： " + res);
                            posLinkIcc.autoResp((byte) slot, true);

                            ApduSendInfo info = new ApduSendInfo();
                            byte[] cmdT = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00};
                            byte[] cmd = StringUtil.hexStrToByteArray(CMD);
                            byte[] datainfo = StringUtil.hexStrToByteArray(Data);
                            info.setDataIn(datainfo);
                            info.setCommand(cmd);
                            info.setLe(Le);
                            info.setLc(Lc);
                            ApduRespInfo info1 = posLinkIcc.isoCommandByApdu((byte) slot, info);
                            byte[] sw = new byte[]{info1.getSwA()};
                            byte[] swb = new byte[]{info1.getSwB()};
                            String SWA = StringUtil.bcdToStr(sw);
                            String SWB = StringUtil.bcdToStr(swb);
                            setText(recvValTxt.getText() + "\n" +"responseCode: "+ SWA + SWB);
                            String data = StringUtil.bcdToStr(info1.getDataOut());
                            setText(recvValTxt.getText() + "\n" +"responseData: "+ data);
                            System.out.print(data);
                            posLinkIcc.close((byte) slot);
                            posLinkIcc.light(false);
                            return;

                        }
                    }
                } catch (PaxIccException e) {
                    UIUtil.showToast(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setText(final String msg) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        recvValTxt.setText(msg);
                    }
                }
        );
    }

    private final Map<String, IccParamValueSetter> PARAM_VALUE_SETTER_MAP = new HashMap<String, IccParamValueSetter>() {
        {
            put("light", new IccParamValueSetter() {
                @Override
                void onSet(Object value) {
                    light = !"false".equals(value);
                }
            });
            put("slot", new IccParamValueSetter() {
                @Override
                void onSet(Object value) {
                    slot = Integer.valueOf((String) value);
                }
            });
            put("CMD", new IccParamValueSetter() {
                @Override
                void onSet(Object value) {
                    CMD = (String) value;
                }
            });
            put("Lc", new IccParamValueSetter() {
                @Override
                void onSet(Object value) {
                    try {
                        Lc = Short.valueOf((String) value);
                    } catch (Exception e) {

                    }
                }
            });
            put("Data", new IccParamValueSetter() {
                @Override
                void onSet(Object value) {
                    Data = (String) value;
                }
            });
            put("Le", new IccParamValueSetter() {
                @Override
                void onSet(Object value) {
                    try {
                        Le = Short.valueOf((String) value);
                    } catch (Exception e) {

                    }
                }
            });
        }
    };

    public static abstract class IccParamValueSetter {
        abstract void onSet(Object value);

    }
}

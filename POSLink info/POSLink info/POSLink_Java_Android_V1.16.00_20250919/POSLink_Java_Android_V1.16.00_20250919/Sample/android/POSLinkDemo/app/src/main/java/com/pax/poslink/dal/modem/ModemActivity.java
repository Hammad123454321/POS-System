package com.pax.poslink.dal.modem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.POSLinkCommon;
import com.pax.poslink.R;
import com.pax.poslink.dal.print.ProcessingDialog;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.peripheries.ModemParameters;
import com.pax.poslink.peripheries.POSLinkModem;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.thread.AppThreadPool;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.SingleButtonEntity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class ModemActivity extends BaseActivity {
    private static final String TAG = "ModemActivity";

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup requestContainer;
    private TextView recvValTxt;

    public static void start(Context context) {
        Intent starter = new Intent(context, ModemActivity.class);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modem);
        requestContainer = (ViewGroup) findViewById(R.id.modem_param_list_container);
        TextView recvNameTxt = (TextView) findViewById(R.id.modem_recv_data_txt).findViewById(R.id.name_txt);
        recvNameTxt.setText("RecvData");
        recvValTxt = (TextView) findViewById(R.id.modem_recv_data_txt).findViewById(R.id.string_val_txt);
        initList();

    }

    private void initList() {
        renderEntityList.clear();
        List<String> baudRateList = Arrays.asList(
                String.valueOf(ModemParameters.BaudRate.BAUD_RATE_1200),
                String.valueOf(ModemParameters.BaudRate.BAUD_RATE_2400),
                String.valueOf(ModemParameters.BaudRate.BAUD_RATE_9600),
                String.valueOf(ModemParameters.BaudRate.BAUD_RATE_14400)
        );
        List<String> serialParamList = Arrays.asList(
                ModemParameters.SerialParam.PARAM_7_EVEN_1,
                ModemParameters.SerialParam.PARAM_7_ODD_1,
                ModemParameters.SerialParam.PARAM_8_EVEN_1,
                ModemParameters.SerialParam.PARAM_8_NONE_1,
                ModemParameters.SerialParam.PARAM_8_ODD_1
        );
        List<String> asyncModeList = Arrays.asList(
                String.valueOf(ModemParameters.AsyncMode.ASYNC),
                String.valueOf(ModemParameters.AsyncMode.SYNC)
        );
        renderEntityList.addAll(
                Arrays.asList(
                        new NameValueStringEntity(ModemParamValueSetter.TEL_NO, "8663048515", InputType.TYPE_CLASS_TEXT, ""),
                        new NameValueSelectEntity(ModemParamValueSetter.BAUD_RATE, baudRateList.get(0), baudRateList, baudRateList),
                        new NameValueSelectEntity(ModemParamValueSetter.SERIAL_PARAM, serialParamList.get(0), serialParamList, serialParamList),
                        new NameValueSelectEntity(ModemParamValueSetter.ASYNC_MODE, asyncModeList.get(0), asyncModeList, asyncModeList),
                        new SingleButtonEntity("Connect", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(final View v, SingleButtonEntity renderEntity) {
                                final Context context = v.getContext();
                                doConnect(context, null);
                            }
                        }),
                        new NameValueBrowserEntity("SendData", "Send", "", InputType.TYPE_CLASS_TEXT, "", new NameValueStringEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, NameValueStringEntity entity) {
                                if (POSLinkModem.getInstance().isConnected()) {
                                    POSLinkModem.getInstance().send(entity.getValue().getBytes());
                                } else {
                                    Toast.makeText(v.getContext(), "Not Connected", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }),
                        new SingleButtonEntity("Recv", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, SingleButtonEntity renderEntity) {
                                recvAndShow(null);
                            }
                        }),
                        new SingleButtonEntity("is Connected", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, SingleButtonEntity renderEntity) {
                                Toast.makeText(v.getContext(), POSLinkModem.getInstance().isConnected() ? "Connected" : "Not Connected", Toast.LENGTH_SHORT).show();
                            }
                        }),
                        new SingleButtonEntity("Disconnect", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, SingleButtonEntity renderEntity) {
                                POSLinkModem.getInstance().disconnect();
                            }
                        })
                )
        );

        if (Convenience.isButtonClickEnough()) {
            renderEntityList.add(createOmahaSingleButtonEntity());
        }

        updateListView();
    }

    private SingleButtonEntity createOmahaSingleButtonEntity() {
        // This is only for internal test.
        return new SingleButtonEntity("Test Omaha Session", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                doConnect(v.getContext(), new AppThreadPool.FinishInMainThreadCallback<POSLinkModem.ConnectResult>() {
                    @Override
                    public void onFinish(POSLinkModem.ConnectResult result) {
                        if (!POSLinkModem.ConnectResult.RESULT_CONNECT_OK.equals(result.getResult())) {
                            return;
                        }
                        final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(v.getContext()));
                        processingDialog.start("Communicating...", false);
                        AppThreadPool.getInstance().postTask(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                String s1c = POSLinkCommon.S_FS;
                                String s1f = POSLinkCommon.S_US;
                                String sendData = Convenience.omahaSendData(s1c, s1f);
                                POSLinkModem.getInstance().reset();
                                boolean sendResult = POSLinkModem.getInstance().send(sendData.getBytes());
                                if (!sendResult) {
                                    Toast.makeText(v.getContext(), "Send fail", Toast.LENGTH_SHORT).show();
                                }
                                final byte[] recvBytes = POSLinkModem.getInstance().recvBlocking(1, 3000);
                                if (recvBytes.length > 0 ) {
                                    POSLinkModem.getInstance().reset();
                                    POSLinkModem.getInstance().send(POSLinkCommon.S_EOT.getBytes());
                                }
                                recvNonBlockingAndShow(new Runnable() {
                                    @Override
                                    public void run() {
                                        POSLinkModem.getInstance().disconnect();
                                    }
                                });
                                return null;
                            }
                        }, new AppThreadPool.FinishInMainThreadCallback<Object>() {
                            @Override
                            public void onFinish(Object result) {
                                processingDialog.dismiss();
                            }
                        });
                    }
                });
            }
        });
    }

    private void doConnect(final Context context, final AppThreadPool.FinishInMainThreadCallback<POSLinkModem.ConnectResult> finishInMainThreadCallback) {
        if (POSLinkModem.getInstance().isConnected()) {
            Toast.makeText(context, "Modem has connected", Toast.LENGTH_SHORT).show();
            return;
        }
        final ModemParameters modemParameters = new ModemParameters();
        for (RenderEntity entity : renderEntityList) {
            if (entity instanceof NameValueStringEntity || entity instanceof NameValueSelectEntity) {
                NameValueEntity<String> nameValueEntity = (NameValueEntity<String>) entity;
                ModemParamValueSetter modemParamValueSetter = ModemParamValueSetter.PARAM_VALUE_SETTER_MAP.get(nameValueEntity.getName());
                if (modemParamValueSetter != null) {
                    modemParamValueSetter.onSet(modemParameters, nameValueEntity.getValue());
                }
            }
        }
        if (TextUtils.isEmpty(modemParameters.getTelephoneNumber())) {
            Toast.makeText(context, "Tel number cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        final ProcessingDialog processingDialog = new ProcessingDialog(new ProgressDialog(context));
        processingDialog.start("Dialing...", false);
        AppThreadPool.getInstance().postTask(new Callable<POSLinkModem.ConnectResult>() {
            @Override
            public POSLinkModem.ConnectResult call() throws Exception {
                POSLinkModem.ConnectResult connect = POSLinkModem.getInstance().connect(getApplicationContext(), modemParameters, 5000);
                return connect;
            }
        }, new AppThreadPool.FinishInMainThreadCallback<POSLinkModem.ConnectResult>() {
            @Override
            public void onFinish(POSLinkModem.ConnectResult result) {
                Toast.makeText(context, "Connected?: " + result.getResult(), Toast.LENGTH_SHORT).show();
                processingDialog.dismiss();
                if (finishInMainThreadCallback != null) {
                    finishInMainThreadCallback.onFinish(result);
                }
            }
        });
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

    private void recvAndShow(final Runnable nothingRecvCallback) {
        AppThreadPool.getInstance().runInBackground(new Runnable() {
            @Override
            public void run() {
                final byte[] recvBytes = POSLinkModem.getInstance().recvBlocking(10, 1000);
                AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recvBytes.length > 0) {
                            try {
                                recvValTxt.setText(recvValTxt.getText() + StringUtil.convert2Hex(new String(recvBytes, "ascii")));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            recvAndShow(nothingRecvCallback);
                        } else {
                            recvValTxt.setText(recvValTxt.getText() + "\n");
                            Toast.makeText(ModemActivity.this, "Nothing receive.", Toast.LENGTH_SHORT).show();
                            if (nothingRecvCallback != null) {
                                nothingRecvCallback.run();
                            }
                        }
                    }
                });
            }
        });
    }


    private void recvNonBlockingAndShow(final Runnable nothingRecvCallback) {
        //Only first time receive non blocking. Just for test this function.
        AppThreadPool.getInstance().runInBackground(new Runnable() {
            private byte[] recvBytes;
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                do {
                    recvBytes = POSLinkModem.getInstance().recvNonBlocking();
                } while (System.currentTimeMillis() - start < 1000 && recvBytes.length <=0);
                AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recvBytes.length > 0) {
                            try {
                                recvValTxt.setText(recvValTxt.getText() + StringUtil.convert2Hex(new String(recvBytes, "ascii")));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            recvNonBlockingAndShow(nothingRecvCallback);
                        } else {
                            recvValTxt.setText(recvValTxt.getText() + "\n");
                            Toast.makeText(ModemActivity.this, "Nothing receive.", Toast.LENGTH_SHORT).show();
                            if (nothingRecvCallback != null) {
                                nothingRecvCallback.run();
                            }
                        }
                    }
                });
            }
        });
    }

    public interface ModemParamValueSetter {
        void onSet(ModemParameters modemParameters, String value);

        String TEL_NO = "TelNo";
        String BAUD_RATE = "BaudRate";
        String SERIAL_PARAM = "SerialParam";
        String ASYNC_MODE = "AsyncMode";

        Map<String, ModemParamValueSetter> PARAM_VALUE_SETTER_MAP = new HashMap<String, ModemParamValueSetter>() {
            {
                put(TEL_NO, new ModemParamValueSetter() {
                    @Override
                    public void onSet(ModemParameters modemParameters, String value) {
                        modemParameters.setTelephoneNumber(value);
                    }
                });
                put(BAUD_RATE, new ModemParamValueSetter() {
                    @Override
                    public void onSet(ModemParameters modemParameters, String value) {
                        modemParameters.setBaudRate(Integer.parseInt(value));
                    }
                });
                put(SERIAL_PARAM, new ModemParamValueSetter() {
                    @Override
                    public void onSet(ModemParameters modemParameters, String value) {
                        modemParameters.setSerialParam(value);
                    }
                });
                put(ASYNC_MODE, new ModemParamValueSetter() {
                    @Override
                    public void onSet(ModemParameters modemParameters, String value) {
                        modemParameters.setAsyncMode(StringUtil.parseInt(value));
                    }
                });
            }
        };
    }


}

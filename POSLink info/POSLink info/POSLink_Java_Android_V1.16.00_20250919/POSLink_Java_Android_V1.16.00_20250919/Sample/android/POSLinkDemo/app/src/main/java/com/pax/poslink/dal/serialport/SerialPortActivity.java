package com.pax.poslink.dal.serialport;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.peripheries.POSLinkSerialPort;
import com.pax.poslink.peripheries.SerialPortParameters;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.thread.AppThreadPool;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.SingleButtonEntity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pax.poslink.util.StringUtil.convert2Hex;

public class SerialPortActivity extends BaseActivity {

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup requestContainer;
    private TextView recvValTxt;
    private POSLinkSerialPort serialPort;

    public static void start(Context context) {
        Intent starter = new Intent(context, SerialPortActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port);
        requestContainer = (ViewGroup) findViewById(R.id.serial_port_param_list_container);
        View dataTxtContainer = findViewById(R.id.serial_port_recv_data_txt);
        TextView recvNameTxt = (TextView) dataTxtContainer.findViewById(R.id.name_txt);
        recvNameTxt.setText("RecvData");
        recvValTxt = (TextView) dataTxtContainer.findViewById(R.id.string_val_txt);

        initList();
    }

    private void initList() {
        List<String> baudRateList = Arrays.asList(
                "1200", "2400", "4800", "9600", "14400",
                "19200", "38400", "43000", "56000", "57600", "115200"
        );

        List<String> dataBits = Arrays.asList("7", "8");
        List<String> parityCheckNames = Arrays.asList("even", "none", "odd");
        List<String> parityCheckValues = Arrays.asList(
                String.valueOf(SerialPortParameters.ParityCheck.EVEN),
                String.valueOf(SerialPortParameters.ParityCheck.NONE),
                String.valueOf(SerialPortParameters.ParityCheck.ODD));
        List<String> stopBitsList = Arrays.asList("1", "2");
        List<String> commPortNames = Arrays.asList("COM1", "COM2", "USBDEV");
        List<String> commPortValues = Arrays.asList(String.valueOf(SerialPortParameters.ComPort.COM1), String.valueOf(SerialPortParameters.ComPort.COM2),
                String.valueOf(SerialPortParameters.ComPort.USBDEV));

        renderEntityList.addAll(
                Arrays.asList(
                        new NameValueSelectEntity(SerialPortParamValueSetter.BAUD_RATE, baudRateList.get(0), baudRateList, baudRateList),
                        new NameValueSelectEntity(SerialPortParamValueSetter.DATA_BITS, dataBits.get(0), dataBits, dataBits),
                        new NameValueSelectEntity(SerialPortParamValueSetter.PARITY_CHECK, parityCheckValues.get(0), parityCheckNames, parityCheckValues),
                        new NameValueSelectEntity(SerialPortParamValueSetter.STOP_BITS, stopBitsList.get(0), stopBitsList, stopBitsList),
                        new NameValueSelectEntity(SerialPortParamValueSetter.COMM_PORT, commPortValues.get(0), commPortNames, commPortValues, Build.MODEL.equals("A80") ? 1 : 0),
                        new SingleButtonEntity("Connect", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(final View v, SingleButtonEntity renderEntity) {
                                final Context context = v.getContext();
                                serialPort = new POSLinkSerialPort(getApplicationContext(), setUpParameters());
                                AppThreadPool.getInstance().runInBackground(new Runnable() {
                                    @Override
                                    public void run() {
                                        POSLinkSerialPort.ConnectResult result = serialPort.connect();
                                        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "Connected: " + result.getResult(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                        }),
                        new NameValueBrowserEntity("SendData", "Send", "abcd", InputType.TYPE_CLASS_TEXT, "", new NameValueStringEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, NameValueStringEntity entity) {
                                if (serialPort == null) {
                                    return;
                                }
                                AppThreadPool.getInstance().runInBackground(new Runnable() {
                                    @Override
                                    public void run() {
                                        serialPort.reset();
                                        serialPort.send(entity.getValue().getBytes());
                                    }
                                });
                            }
                        }),
                        new SingleButtonEntity("SendFile", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, SingleButtonEntity renderEntity) {
                                try {
                                    InputStream is = getAssets().open("Data.txt");
                                    byte[] fileBytes = new byte[is.available()];
                                    is.read(fileBytes);
                                    is.close();
                                    if(serialPort == null){
                                        Toast.makeText(getApplicationContext(), "Not connect, seralPort is null ", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    AppThreadPool.getInstance().runInBackground(new Runnable() {
                                        @Override
                                        public void run() {
                                            serialPort.send(fileBytes);
                                        }
                                    });
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }),
                        new SingleButtonEntity("Recv", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, SingleButtonEntity renderEntity) {
                                recvAndShow();
                            }
                        }),
                        new SingleButtonEntity("Disconnect", new SingleButtonEntity.ClickCallback() {
                            @Override
                            public void onClick(View v, SingleButtonEntity renderEntity) {
                                AppThreadPool.getInstance().runInBackground(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (serialPort != null) {
                                            serialPort.disconnect();
                                            serialPort = null;
                                        }
                                    }
                                });

                            }
                        })
                )
        );

        updateListView();
    }

    private void recvAndShow() {
        if (serialPort == null) {
            return;
        }
        AppThreadPool.getInstance().runInBackground(new Runnable() {
            @Override
            public void run() {
                final byte[] recvBytes = serialPort.recvBlocking(1, 1000);
                AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (recvBytes.length > 0) {
                            try {
                                recvValTxt.setText(recvValTxt.getText() + convert2Hex(new String(recvBytes, "ascii")));
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            recvAndShow();
                        } else {
                            recvValTxt.setText(recvValTxt.getText() + "\n");
                            Toast.makeText(SerialPortActivity.this, "Nothing receive.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private SerialPortParameters setUpParameters() {
        final SerialPortParameters modemParameters = new SerialPortParameters();
        for (RenderEntity entity : renderEntityList) {
            if (entity instanceof NameValueStringEntity || entity instanceof NameValueSelectEntity) {
                NameValueEntity<String> nameValueEntity = (NameValueEntity<String>) entity;
                SerialPortParamValueSetter serialPortParamValueSetter = SerialPortParamValueSetter.PARAM_VALUE_SETTER_MAP.get(nameValueEntity.getName());
                if (serialPortParamValueSetter != null) {
                    serialPortParamValueSetter.onSet(modemParameters, nameValueEntity.getValue());
                }
            }
        }
        return modemParameters;
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


    public static abstract class SerialPortParamValueSetter {
        abstract void onSet(SerialPortParameters parameters, String value);

        static final String BAUD_RATE = "BaudRate";
        static final String DATA_BITS = "DataBits";
        static final String PARITY_CHECK = "ParityCheck";
        static final String STOP_BITS = "StopBits";
        static final String COMM_PORT = "ComPort";

        static final Map<String, SerialPortParamValueSetter> PARAM_VALUE_SETTER_MAP = new HashMap<>();

        static {
            PARAM_VALUE_SETTER_MAP.put(BAUD_RATE, new SerialPortParamValueSetter() {
                @Override
                void onSet(SerialPortParameters parameters, String value) {
                    parameters.setBaudRate(Integer.parseInt(value));
                }
            });
            PARAM_VALUE_SETTER_MAP.put(DATA_BITS, new SerialPortParamValueSetter() {
                @Override
                void onSet(SerialPortParameters parameters, String value) {
                    parameters.setDataBits(Integer.parseInt(value));
                }
            });
            PARAM_VALUE_SETTER_MAP.put(PARITY_CHECK, new SerialPortParamValueSetter() {
                @Override
                void onSet(SerialPortParameters parameters, String value) {
                    parameters.setParityCheck(Integer.parseInt(value));
                }
            });
            PARAM_VALUE_SETTER_MAP.put(STOP_BITS, new SerialPortParamValueSetter() {
                @Override
                void onSet(SerialPortParameters parameters, String value) {
                    parameters.setStopBits(Integer.parseInt(value));
                }
            });
            PARAM_VALUE_SETTER_MAP.put(COMM_PORT, new SerialPortParamValueSetter() {
                @Override
                void onSet(SerialPortParameters parameters, String value) {
                    parameters.setComPort(Integer.parseInt(value));
                }
            });
        }
    }
}

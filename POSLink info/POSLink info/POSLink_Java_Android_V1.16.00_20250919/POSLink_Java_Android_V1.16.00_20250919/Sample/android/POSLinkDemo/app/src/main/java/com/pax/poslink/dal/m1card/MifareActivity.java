package com.pax.poslink.dal.m1card;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.CommSetting;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.exceptions.PiccException;
import com.pax.poslink.peripheries.PiccManager;
import com.pax.poslink.peripheries.tech.CardInfo;
import com.pax.poslink.peripheries.tech.MifareClassic;
import com.pax.poslink.peripheries.tech.MifareUltralight;
import com.pax.poslink.peripheries.tech.Technology;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.SingleButtonEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * Created by Justin.Z on 2020-1-19
 */
public class MifareActivity extends BaseActivity {

    private static final String MIFARE_Classic = "MIFARE Classic";
    private static final String MIFARE_Ultralight = "MIFARE Ultralight";

    private List<RenderEntity> renderEntityList = new ArrayList<>();
    private ViewGroup requestContainer;
    private TextView recvValTxt;
    private TextView tvInfo;
    private Spinner cmdEdit = null;

    private String pwdType;
    private String password;
    private String rawData;
    private int blockNo, updateBlockNo;
    private int optionType;
    private byte[] aucKey;
    private String blockValue;
    private MifareClassic.OperationCommand m1OperationCommand;
    private Dialog processingDialog;
    private int cardReaderType;
    private int mifareType;
    private boolean usePwd;
    private boolean isCancel;

    private PiccManager piccManager;

    private static final List<String> CMDS = new ArrayList<String>() {
        {
            addAll(Arrays.asList(
                    MIFARE_Classic, MIFARE_Ultralight));
        }
    };

    private final Map<String, List<RenderEntity>> commandMapRenderList = new HashMap<String, List<RenderEntity>>() {
        {
            List<String> type = Arrays.asList("Internal", "External");
            List<String> options = Arrays.asList("read", "write", "operate", "transceive");
            List<String> passwordType = Arrays.asList("A", "B");
            List<String> operateType = Arrays.asList("INCREASE", "DECREASE", "BACKUP");
            put(MIFARE_Classic, Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity("card reader", type.get(0), type, type),
                    new NameValueSelectEntity("option", options.get(0), options, options),
                    new NameValueSelectEntity("passwordType", passwordType.get(0), passwordType, passwordType),
                    new NameValueSelectEntity("operateType", operateType.get(0), operateType, operateType),
                    new NameValueStringEntity("password", "FFFFFFFFFFFF", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity("blockNo", "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity("blockValue", "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity("updateBlockNo", "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity("raw data", "", InputType.TYPE_CLASS_TEXT, ""),
                    new SingleButtonEntity("process", new SingleButtonEntity.ClickCallback() {
                        @Override
                        public void onClick(final View v, SingleButtonEntity renderEntity) {
                            process();
                        }
                    })
            ));

            List<String> optionsUltralight = Arrays.asList("read", "write", "transceive");
            List<String> optionsPwdAuth = Arrays.asList("Y", "N");
            put(MIFARE_Ultralight, Arrays.<RenderEntity>asList(
                    new NameValueSelectEntity("card reader", type.get(0), type, type),
                    new NameValueSelectEntity("option", optionsUltralight.get(0), optionsUltralight, optionsUltralight),
                    new NameValueStringEntity("blockNo", "0", InputType.TYPE_CLASS_NUMBER, ""),
                    new NameValueStringEntity("blockValue", "", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueSelectEntity("PWD Auth", optionsPwdAuth.get(0), optionsPwdAuth, optionsPwdAuth),
                    new NameValueStringEntity("aucKey", "49454D4B41455242214E4143554F5946", InputType.TYPE_CLASS_TEXT, ""),
                    new NameValueStringEntity("raw data", "", InputType.TYPE_CLASS_TEXT, ""),
                    new SingleButtonEntity("process", new SingleButtonEntity.ClickCallback() {
                        @Override
                        public void onClick(final View v, SingleButtonEntity renderEntity) {
                            process();
                        }
                    })
            ));
        }
    };


    public static void start(Context context) {
        Intent starter = new Intent(context, MifareActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mifare);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (processingDialog != null) {
            processingDialog.dismiss();
            processingDialog = null;
        }
    }

    private void initView() {
        requestContainer = (ViewGroup) findViewById(R.id.icc_param_list_container);
        View dataTxtContainer = findViewById(R.id.serial_port_recv_data_txt);
        TextView recvNameTxt = (TextView) dataTxtContainer.findViewById(R.id.name_txt);
        recvNameTxt.setText("RecvData");
        recvValTxt = (TextView) dataTxtContainer.findViewById(R.id.string_val_txt);
        tvInfo = findViewById(R.id.tv_cardinfo);
        piccManager = PiccManager.getInstance(getApplicationContext());

        cmdEdit = findViewById(R.id.mifare_card_type);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CMDS);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cmdEdit.setAdapter(arrayAdapter);
        cmdEdit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mifareType = position;
                String command = CMDS.get(position);
                showCorrespondingRequestView(command);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showCorrespondingRequestView(String command) {
        renderEntityList.clear();
        requestContainer.removeAllViews();

        List<RenderEntity> commandRenderList = commandMapRenderList.get(command);
        if (commandRenderList != null) {
            renderEntityList.addAll(commandRenderList);
        }
        for (Object obj : renderEntityList) {
            if (obj instanceof RenderEntity) {
                RenderEntity renderEntity = (RenderEntity) obj;
                CommonItemView itemView = renderEntity.createView(requestContainer);
                requestContainer.addView(itemView.getView());
                itemView.getView().setTag(itemView);
                itemView.render(renderEntity);
            }
        }
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
        isCancel = false;
        setUpParameters();
        if (!verifyData()) {
            return;
        }
        recvValTxt.setText("");
        processingDialog = ProcessProgressDialog.createDialog(this, this.getString(R.string.pls_tap_mifare_card), true, false,
                new ProcessProgressDialog.OnSetListener() {
                    @Override
                    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
                        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel Process", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isCancel = true;
                            }
                        });
                    }
                });
        processingDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (optionType) {
                    case 0:
                        read();
                        break;
                    case 1:
                        write();
                        break;
                    case 2:
                        operate();
                        break;
                    case 3:
                        transceive();
                        break;
                }
            }
        }).start();
    }

    private boolean verifyData() {
        if (blockNo < 0 || blockNo > 256) {
            UIUtil.showToast(getApplicationContext(), " block out of bounds:" + blockNo, Toast.LENGTH_SHORT);
            return false;
        }
        if (optionType == 1 && mifareType == 0) {
            if (hexStrToByteArray(blockValue).length != 16) {
                UIUtil.showToast(getApplicationContext(), "must write 16-bytes", Toast.LENGTH_SHORT);
                return false;
            }
        }

        if (optionType == 2) {
            if (StringUtil.parseInt(blockValue) <= 0) {
                UIUtil.showToast(getApplicationContext(), "Please re-enter the value", Toast.LENGTH_SHORT);
                return false;
            }

            if (updateBlockNo < 0 || updateBlockNo > 256) {
                UIUtil.showToast(getApplicationContext(), " block out of bounds:" + updateBlockNo, Toast.LENGTH_SHORT);
                return false;
            }
        }

        return true;
    }

    private void read() {
        processMifare(new DetectCallBack() {
            @Override
            public void onSucc(final MifareClassic mifareClassic) {
                try {
                    byte[] bytes = mifareClassic.readBlock(blockNo);
                    setText(bcdToStr(bytes));
                    hideDialog();
                } catch (PiccException e) {
                    e.printStackTrace();
                    UIUtil.showToast(getApplicationContext(), e.getErrCode() + " , " + e.getErrMsg(), Toast.LENGTH_SHORT);
                    hideDialog();
                }
            }

            @Override
            public void onSucc(MifareUltralight mifareUltralight) {
                try {
                    byte[] ret = mifareUltralight.readPages(blockNo);
                    setText(bcdToStr(ret));
                    hideDialog();
                } catch (PiccException e) {
                    e.printStackTrace();
                    UIUtil.showToast(getApplicationContext(), e.getErrCode() + " , " + e.getErrMsg(), Toast.LENGTH_SHORT);
                    hideDialog();
                }
            }
        });
    }

    private void write() {
        processMifare(new DetectCallBack() {
            @Override
            public void onSucc(final MifareClassic mifareClassic) {
                try {
                    mifareClassic.writeBlock(blockNo, hexStrToByteArray(blockValue));
                    hideDialog();
                    UIUtil.showToast(getApplicationContext(), "write success", Toast.LENGTH_SHORT);
                } catch (PiccException e) {
                    e.printStackTrace();
                    UIUtil.showToast(getApplicationContext(), e.getErrMsg(), Toast.LENGTH_SHORT);
                    hideDialog();
                }
            }

            @Override
            public void onSucc(MifareUltralight mifareUltralight) {
                try {
                    mifareUltralight.writePage(blockNo, hexStrToByteArray(blockValue));
                    hideDialog();
                    UIUtil.showToast(getApplicationContext(), "write success", Toast.LENGTH_SHORT);
                } catch (PiccException e) {
                    e.printStackTrace();
                    UIUtil.showToast(getApplicationContext(), e.getErrCode() + " , " + e.getErrMsg(), Toast.LENGTH_SHORT);
                    hideDialog();
                }
            }
        });
    }

    private void operate() {
        processMifare(new DetectCallBack() {
            @Override
            public void onSucc(MifareClassic mifareClassic) {
                try {
                    mifareClassic.operate(blockNo, Integer.valueOf(blockValue), updateBlockNo, m1OperationCommand);
                    hideDialog();
                } catch (PiccException e) {
                    e.printStackTrace();
                    UIUtil.showToast(getApplicationContext(), e.getErrMsg(), Toast.LENGTH_SHORT);
                    hideDialog();
                }
            }

            @Override
            public void onSucc(MifareUltralight mifareUltralight) {

            }
        });
    }

    private void transceive() {
        processMifare(new DetectCallBack() {
            @Override
            public void onSucc(MifareClassic mifareClassic) {
                try {
                    byte[] bytes = mifareClassic.transceive(hexStrToByteArray(rawData));
                    setText(bcdToStr(bytes));
                    hideDialog();
                } catch (PiccException e) {
                    e.printStackTrace();
                    UIUtil.showToast(getApplicationContext(), e.getErrMsg(), Toast.LENGTH_SHORT);
                    hideDialog();
                }
            }

            @Override
            public void onSucc(MifareUltralight mifareUltralight) {
                try {
                    byte[] bytes = mifareUltralight.transceive(hexStrToByteArray(rawData));
                    setText(bcdToStr(bytes));
                    hideDialog();
                } catch (PiccException e) {
                    e.printStackTrace();
                    UIUtil.showToast(getApplicationContext(), e.getErrMsg(), Toast.LENGTH_SHORT);
                    hideDialog();
                }
            }
        });
    }

    private void processMifare(DetectCallBack detectCallBack) {
        if (cardReaderType == 0) {
            try {
                piccManager.open();
                while (true) {
                    if (isCancel) {
                        break;
                    }

                    CardInfo cardInfo = piccManager.detect(PiccManager.DetectMode.ONLY_M);
                    if (cardInfo == null) {
                        try {
                            sleep(300);
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        continue;
                    }
                    if (cardInfo.hasTech(Technology.MIFARE_CLASSIC) && mifareType == 0) {
                        final MifareClassic mifareClassic = MifareClassic.get(cardInfo);
                        if (optionType == 3) {
                            detectCallBack.onSucc(mifareClassic);
                            return;
                        }
                        if (authenticate(mifareClassic)) {
                            detectCallBack.onSucc(mifareClassic);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String info = "CardType: " + getMifareType(mifareClassic.getType()) + "\n"
                                            + "CardSize: " + mifareClassic.getSize() + "\n"
                                            + "SectorCount: " + mifareClassic.getSectorCount() + "\n"
                                            + "BlockCount: " + mifareClassic.getBlockCount();
                                    tvInfo.setText(info);
                                }
                            });
                        } else {
                            UIUtil.showToast(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT);
                            hideDialog();
                        }
                    } else if (cardInfo.hasTech(Technology.MIFARE_ULTRALIGHT) && mifareType == 1) {
                        final MifareUltralight mifareUltralight = MifareUltralight.get(cardInfo);
                        if (optionType == 3) {
                            detectCallBack.onSucc(mifareUltralight);
                            return;
                        }
                        if (usePwd) {
                            if (!mifareUltralight.authentication(aucKey)) {
                                hideDialog();
                                UIUtil.showToast(getApplicationContext(), "ultralightAuthentication error", Toast.LENGTH_SHORT);
                                return;
                            }
                        }
                        detectCallBack.onSucc(mifareUltralight);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String info = "CardType: " + getMifareUltralightType(mifareUltralight.getType());
                                tvInfo.setText(info);
                            }
                        });
                    } else {
                        UIUtil.showToast(getApplicationContext(), String.format("Not %s card", mifareType == 0 ? "MIFARE CLASSIC" : "MIFARE ULTRALIGHT"), Toast.LENGTH_SHORT);
                        hideDialog();
                    }
                    break;
                }
                piccManager.close();
            } catch (PiccException e) {
                e.printStackTrace();
                UIUtil.showToast(getApplicationContext(), e.getErrMsg(), Toast.LENGTH_SHORT);
                hideDialog();
            }
        } else {
            if (mifareType == 1) {
                UIUtil.showToast(getApplicationContext(), "Remote card reader does not support Ultralight now", Toast.LENGTH_SHORT);
                hideDialog();
            } else {
                MifareClassic.RemoteParam remoteParam = new MifareClassic.RemoteParam();
                CommSetting commSetting = SettingINI.getCommSettingFromFile(this.getFilesDir().getPath() + "/" + SettingINI.FILENAME);
                remoteParam.setCommSetting(commSetting);
                remoteParam.setPassword("FFFFFFFFFFFF");
                remoteParam.setTimeout(3000);
                remoteParam.setPasswordType(0);
                final MifareClassic mifareClassic = MifareClassic.get(this, remoteParam);
                detectCallBack.onSucc(mifareClassic);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String info = "CardType: " + getMifareType(mifareClassic.getType()) + "\n"
                                + "CardSize: " + mifareClassic.getSize() + "\n"
                                + "SectorCount: " + mifareClassic.getSectorCount() + "\n"
                                + "BlockCount: " + mifareClassic.getBlockCount();
                        tvInfo.setText(info);
                    }
                });
            }
        }
    }

    private boolean authenticate(MifareClassic mifareClassic) {
        if (mifareClassic == null) {
            return false;
        }

        if ("A".equals(pwdType)) {
            return mifareClassic.authenticateSectorWithKeyA(mifareClassic.blockToSector(blockNo), hexStrToByteArray(password));
        } else {
            return mifareClassic.authenticateSectorWithKeyB(mifareClassic.blockToSector(blockNo), hexStrToByteArray(password));
        }
    }

    private String getMifareType(int type) {
        switch (type) {
            case MifareClassic.TYPE_CLASSIC:
                return "MIFARE Classic";
            case MifareClassic.TYPE_PLUS:
                return "MIFARE PLUS";
            case MifareClassic.TYPE_PRO:
                return "MIFARE Pro";
            default:
                return "Unknown";
        }
    }

    private String getMifareUltralightType(int type) {
        switch (type) {
            case MifareUltralight.TYPE_ULTRALIGHT:
                return "MIFARE ULTRALIGHT";
            case MifareUltralight.TYPE_ULTRALIGHT_C:
                return "MIFARE ULTRALIGHT C";
            case MifareUltralight.TYPE_ULTRALIGHT_EV1:
                return "MIFARE ULTRALIGHT EV1";
            default:
                return "Unknown";
        }
    }

    interface DetectCallBack {
        void onSucc(MifareClassic mifareClassic);

        void onSucc(MifareUltralight mifareUltralight);
    }

    private void hideDialog() {
        if (processingDialog != null)
            processingDialog.dismiss();
    }

    private static byte[] hexStrToByteArray(String str) {
        if (str == null) {
            return new byte[0];
        }

        if (str.length() == 0) {
            return new byte[0];
        }
        byte[] byteArray = new byte[str.length() / 2];
        for (int i = 0; i < byteArray.length; i++) {
            String subStr = str.substring(2 * i, 2 * i + 2);
            byteArray[i] = ((byte) Integer.parseInt(subStr, 16));
        }
        return byteArray;
    }

    private String bcdToStr(byte[] b) throws IllegalArgumentException {
        if (b == null) {
            throw new IllegalArgumentException("bcdToStr input arg is null");
        }

        char HEX_DIGITS[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_DIGITS[(b[i] & 0xf0) >>> 4]);
            sb.append(HEX_DIGITS[b[i] & 0x0f]);
        }

        return sb.toString();
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

    private final Map<String, MifareParamValueSetter> PARAM_VALUE_SETTER_MAP = new HashMap<String, MifareParamValueSetter>() {
        {
            put("passwordType", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    pwdType = (String) value;
                }
            });
            put("blockNo", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    blockNo = StringUtil.parseInt((String) value);
                }
            });
            put("password", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    password = (String) value;
                }
            });
            put("option", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    if ("read".equals(value)) {
                        optionType = 0;
                    } else if ("write".equals(value)) {
                        optionType = 1;
                    } else if ("operate".equals(value)) {
                        optionType = 2;
                    } else if ("transceive".equals(value)) {
                        optionType = 3;
                    }
                }
            });
            put("operateType", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    if ("INCREASE".equals(value)) {
                        m1OperationCommand = MifareClassic.OperationCommand.OPERATION_INCREASE;
                    } else if ("DECREASE".equals(value)) {
                        m1OperationCommand = MifareClassic.OperationCommand.OPERATION_DECREASE;
                    } else if ("BACKUP".equals(value)) {
                        m1OperationCommand = MifareClassic.OperationCommand.OPERATION_BACKUP;
                    }
                }
            });
            put("blockValue", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    blockValue = (String) value;
                }
            });
            put("updateBlockNo", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    updateBlockNo = StringUtil.parseInt((String) value);
                }
            });
            put("aucKey", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    aucKey = hexStrToByteArray((String) value);
                }
            });
            put("card reader", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    if ("Internal".equals(value)) {
                        cardReaderType = 0;
                    } else {
                        cardReaderType = 1;
                    }
                }
            });

            put("PWD Auth", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    if ("Y".equals(value)) {
                        usePwd = true;
                    } else {
                        usePwd = false;
                    }
                }
            });
            put("raw data", new MifareParamValueSetter() {
                @Override
                void onSet(Object value) {
                    rawData = (String) value;
                }
            });
        }

    };

    static abstract class MifareParamValueSetter {
        abstract void onSet(Object value);
    }
}

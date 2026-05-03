package com.pax.poslink.ui.comsetting;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.pax.poslink.AsyncPosLinkTask;
import com.pax.poslink.CommSetting;
import com.pax.poslink.POSLinkAndroid;
import com.pax.poslink.ui.MainActivity;
import com.pax.poslink.util.LogStaticWrapper;
import com.pax.poslink.util.PermissionUtil;
import com.pax.poslink.widget.ProcessProgressDialog;
import com.pax.poslink.R;
import com.pax.poslink.SettingINI;
import com.pax.poslink.WebService;
import com.pax.poslink.broadpos.BroadPOSCommunicator;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.usb.UsbUtil;
import com.pax.poslink.ui.base.TaskFragment;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.view.DialogUtils;

import java.util.List;

public class CommSettingFragment extends TaskFragment implements OnClickListener,OnTouchListener, GetDeviceIPDialogFragment.OnGetDeviceIPDialogListener, ProcessProgressDialog.OnSetListener {
    private static final String defErrMsg = "Cannot get result from server";
    private static final String ON = "ON";

    private Spinner mEdtCommType = null;

    private EditText mEdtTimeOut = null;

    private EditText mEdtSerialPort = null;

    private Spinner mEdtBaudRate = null;
    private Spinner mEdtHost = null;
    private View setHostContainer;
    private Spinner mEdtEnableProxy = null;

    private EditText mEdtDestIP = null;

    private EditText mEdtDestPort = null;

    private EditText mEdtMacAddr = null;
    private TextView mEdtDevice = null;

    private final int REQUEST_BT_ENABLE = 1;
    private final int REQUEST_BT_DISCOVER = 2;

    private WebService mGetIpFromWeb = new WebService("www.poslink.com/ws/process2.asmx/GetDeviceLocalIP");
    private View rootView;

    public static CommSettingFragment newInstance() {

        Bundle args = new Bundle();

        CommSettingFragment fragment = new CommSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTask();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.comm_setting, container, false);


        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        mEdtCommType = (Spinner)rootView.findViewById(R.id.commSetting_commType);
        mEdtCommType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mEdtCommType.getItemAtPosition(position).toString().equals(CommSetting.USB)) {
                    whenUSB(parent);
                } else if (mEdtCommType.getItemAtPosition(position).toString().equals(CommSetting.BT)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PermissionUtil.askPermission(getActivity(), new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, new PermissionUtil.PermissionGrantCallback() {
                            @Override
                            public void onRefused() {
                                DialogUtils.showWarnDialog(getActivity(), "Request Permission", "Application may not work properly", true);
                            }
                        });
                    }
                }
            }

            private void whenUSB(AdapterView<?> parent) {
                UsbDevice usbDevice = UsbUtil.getDevice(parent.getContext());
                if (usbDevice != null) {
                    mEdtDevice.setText(usbDevice.getDeviceName());
                    UsbUtil.requestUSBPermissionIfNeed(parent.getContext(), usbDevice);
                }else {
                    UsbUtil.requestUSBPermissionIfNeed(parent.getContext());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (mEdtCommType.getSelectedItem().toString().equals(CommSetting.USB)) {
                    whenUSB(parent);
                }
            }
        });

        /* 20120209 QC Bug No30 CombBox End */
        mEdtTimeOut = (EditText)rootView.findViewById(R.id.commSetting_timeout);

        mEdtSerialPort = (EditText)rootView.findViewById(R.id.commSetting_serialPort);
        mEdtBaudRate = (Spinner)rootView.findViewById(R.id.commSetting_baudRate);
        mEdtDestIP = (EditText)rootView.findViewById(R.id.commSetting_destIP);
        mEdtDestPort = (EditText)rootView.findViewById(R.id.commSetting_destPort);
        mEdtMacAddr = (EditText)rootView.findViewById(R.id.commSetting_mac);
        setHostContainer = rootView.findViewById(R.id.commsetting_host_container);
        mEdtHost = (Spinner)rootView.findViewById(R.id.commSetting_host);
        setHostContainer.setVisibility(View.GONE);
        mEdtEnableProxy = (Spinner)rootView.findViewById(R.id.comm_setting_enable_proxy);
        mEdtMacAddr.setOnTouchListener(this);

        /* 20120209 QC Bug No30 CombBox End */
        Button mBtnSet = (Button)rootView.findViewById(R.id.commSetting_btn_set);
        mBtnSet.setOnClickListener(this);

        Button mBtnGetIP = (Button)rootView.findViewById(R.id.commSetting_btn_getSN);
        mBtnGetIP.setOnClickListener(this);

        Button mBtnScan = (Button)rootView.findViewById(R.id.commSetting_btn_scan);
        mBtnScan.setOnClickListener(this);

        Button mBtnStartListener = (Button)rootView.findViewById(R.id.commSetting_btn_startlistener);
        mBtnStartListener.setOnClickListener(this);

        Button mBtnStopListener = (Button)rootView.findViewById(R.id.commSetting_btn_stoplistener);
        mBtnStopListener.setOnClickListener(this);

        mEdtDevice = rootView.findViewById(R.id.commSetting_device);
        mEdtDevice.setOnClickListener(this);

        String iniFile = getActivity().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        CommSetting commset = SettingINI.getCommSettingFromFile(iniFile);

        int index = UIUtil.findStringId(getResources().getStringArray(R.array.commSetting_types), commset.getType());
        if (index == -1)
        {
            mEdtCommType.setSelection(1);
            mEdtTimeOut.setText("60000");
            mEdtSerialPort.setText("COM1");
            mEdtBaudRate.setSelection(0);
            mEdtDestIP.setText("127.0.0.1");
            mEdtDestPort.setText("10009");
            mEdtHost.setSelection(0);
            mEdtEnableProxy.setSelection(0);

            commset.setType(mEdtCommType.getSelectedItem().toString());
            commset.setTimeOut(mEdtTimeOut.getText().toString());
            commset.setSerialPort(mEdtSerialPort.getText().toString());
            commset.setBaudRate(mEdtBaudRate.getSelectedItem().toString());
            commset.setDestIP(mEdtDestIP.getText().toString());
            commset.setDestPort(mEdtDestPort.getText().toString());
            commset.setMacAddr("");
            Object selectedHost = mEdtHost.getSelectedItem();
            Convenience.setHost(getContext(), commset, selectedHost == null ? "" : selectedHost.toString());
            commset.setEnableProxy(ON.equalsIgnoreCase(mEdtEnableProxy.getSelectedItem().toString()));
            SettingINI.saveCommSettingToFile(iniFile, commset);
        }
        else
        {
            mEdtCommType.setSelection(index);
            mEdtTimeOut.setText(commset.getTimeOut());
            mEdtSerialPort.setText(commset.getSerialPort());
            int indexBaudRate = UIUtil.findStringId(getResources().getStringArray(R.array.commSetting_baudRate), commset.getBaudRate());
            mEdtBaudRate.setSelection(indexBaudRate);
            mEdtDestIP.setText(commset.getDestIP());
            mEdtDestPort.setText(commset.getDestPort());
            mEdtMacAddr.setText(commset.getMacAddr());
            int indexHost = UIUtil.findStringId(getResources().getStringArray(R.array.commSetting_host), Convenience.getHost(getContext(), commset));
            mEdtHost.setSelection(indexHost);
            int indexEnableProxy = UIUtil.findStringId(getResources().getStringArray(R.array.comm_setting_enable_proxy), commset.isEnableProxy() ? ON : "OFF");
            mEdtEnableProxy.setSelection(indexEnableProxy);
        }
    }

    private void setCommSetting(){
        //create commsetting object
        String iniFile = getActivity().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        CommSetting commset = SettingINI.getCommSettingFromFile(iniFile);

        //initialization value  for comsetting's attribute
        commset.setType(mEdtCommType.getSelectedItem().toString());
        commset.setTimeOut(mEdtTimeOut.getText().toString());
        commset.setSerialPort(mEdtSerialPort.getText().toString());
        commset.setBaudRate(mEdtBaudRate.getSelectedItem().toString());
        commset.setDestIP(mEdtDestIP.getText().toString());
        commset.setDestPort(mEdtDestPort.getText().toString());
        commset.setMacAddr(mEdtMacAddr.getText().toString());
        commset.setDeviceName(mEdtDevice.getText().toString());
        Object selectedHost = mEdtHost.getSelectedItem();
        Convenience.setHost(getContext(), commset, selectedHost == null ? "" : selectedHost.toString());
        commset.setEnableProxy(ON.equalsIgnoreCase(mEdtEnableProxy.getSelectedItem().toString()));

        Log.i(TAG, "coms.CommType = " + commset.getType() + "; coms.TimeOut=" + commset.getTimeOut()
                + "; SerialPort=" + commset.getSerialPort() + "; coms.BaudRate=" + commset.getBaudRate()
                + "; coms.DestIP=" + commset.getDestIP() + "; coms.DestPort=" + commset.getDestPort()
                + "; coms.MacAddr=" + commset.getMacAddr() + "; coms.Host=" + Convenience.getHost(getContext(), commset) + "; coms.EnableProxy=" + commset.isEnableProxy());
        POSLinkAndroid.initPOSListener(getContext(), commset);
        SettingINI.saveCommSettingToFile(iniFile, commset);

    }

    @Override
    public void handleMessage(Message msg) {
        String title = msg.getData().getString(Constant.DIALOG_TITLE);
        String message = msg.getData().getString(Constant.DIALOG_MESSAGE);
        switch (msg.what) {
            case Constant.TRANSACTION_SUCCESSED:
                mEdtDestIP.setText(msg.getData().getString(getResources().getString(R.string.commSetting_destIP)));
                mEdtDestPort.setText(msg.getData().getString(getResources().getString(R.string.commSetting_destPort)));
                mEdtMacAddr.setText(msg.getData().getString(getResources().getString(R.string.commSetting_mac)));
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
            case Constant.TRANSACTION_FAILURE:
                DialogUtils.showMsgDialog(getActivity(), title, message);
                break;
        }
    }

    private void process() {
        try {
            Thread.sleep(500);
            mGetIpFromWeb.process();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LogStaticWrapper.getLog().exceptionLog(e);
        }
    }

    private void getIPFromWeb() {
        String iniFile = getActivity().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        GetDeviceIPDialogFragment getIPFromWebDialog = GetDeviceIPDialogFragment.newInstance("Please enter SN & Token:",
                SettingINI.getLastTermId(iniFile),
                SettingINI.getLastSN(iniFile));
        getIPFromWebDialog.setOnGetDeviceIPDialogListener(this);
        getIPFromWebDialog.show(getFragmentManager(), "getIPFromWeb");
        getFragmentManager().beginTransaction().commit();
    }

    @Override
    public void onGetDeviceIPDialogComplete(String termId, String sn, String token) {
        String iniFile = getActivity().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        SettingINI.saveLastSN(iniFile, sn);
        SettingINI.saveLastTermId(iniFile, termId);

        mGetIpFromWeb.setRequestVar("TerminalId", termId);
        mGetIpFromWeb.setRequestVar("Token", token);
        mGetIpFromWeb.setRequestVar("SerialNo", sn);

        setTask();
        if (mTask == null) {
            mTask = new AsyncPosLinkTask(this);
            dataFragment.setData(mTask);
            mTask.execute();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.commSetting_btn_set:
                setCommSetting();
                break;
            case R.id.commSetting_btn_getSN:
                getIPFromWeb();
                break;
            case R.id.commSetting_btn_scan://add by sunny
                BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                if (btAdapter == null) {
                    UIUtil.showToast(getContext(), "Bluetooth is not available!", Toast.LENGTH_SHORT);
                } else {
                    Intent intent;
                    if (!btAdapter.isEnabled()) {
                        intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, REQUEST_BT_ENABLE);
                    } else {
                        intent = new Intent(getActivity(), BluetoothDeviceListActivity.class);
                        startActivityForResult(intent, REQUEST_BT_DISCOVER);
                    }
                }

                break;
            case R.id.commSetting_btn_startlistener:
                BroadPOSCommunicator.getInstance(getActivity()).startListeningService(new BroadPOSCommunicator.StartListenerCallBack() {
                    @Override
                    public void onSuccess() {
                        UIUtil.showToast(getActivity(), "Success", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onFail(String msg) {
                        UIUtil.showToast(getActivity(), msg, Toast.LENGTH_SHORT);
                    }
                });
                break;
            case R.id.commSetting_btn_stoplistener:
                BroadPOSCommunicator.getInstance(getActivity()).stopListeningService();
                break;
            case R.id.commSetting_device:
                showDeviceList();
                break;
        }
    }

    private void showDeviceList() {
        final List<UsbDevice> blueItems = UsbUtil.getDevices(getActivity());
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(getActivity(), blueItems);
        View view = (getActivity()).getLayoutInflater().inflate(R.layout.listview_dialog, null);
        ListView listView = (ListView)view.findViewById(R.id.listView);
        listView.setAdapter(deviceListAdapter);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setOnCancelListener(null);
        builder.setTitle("Device List");
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UsbDevice usbDevice = blueItems.get(position);
                UsbUtil.requestUSBPermissionIfNeed(parent.getContext(), usbDevice);
                mEdtDevice.setText(usbDevice.getDeviceName());
                setCommSetting();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_BT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getActivity(), BluetoothDeviceListActivity.class);
                    startActivityForResult(intent, REQUEST_BT_DISCOVER);
                }
                break;

            case REQUEST_BT_DISCOVER: // bt scan
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    mEdtMacAddr.setText(address);
                }

                break;
        }
    }

    @Override
    public void run() {
        // processTransactions
        process();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) { }

    @Override
    public void onTaskCompleted() {
        super.onTaskCompleted();
        String mIPResult = mGetIpFromWeb.getResponseVar("ResultCode");
        if (mIPResult == null || mIPResult.length() == 0) {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_FAILURE;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, "ERROR");
            b.putString(Constant.DIALOG_MESSAGE, defErrMsg);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }
        else if(!mIPResult.equals("0"))
        {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_FAILURE;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, "ERROR");
            String resultMsg = mGetIpFromWeb.getResponseVar("ResultMsg");
            if(resultMsg.length() == 0){
                resultMsg = defErrMsg;
            }
            b.putString(Constant.DIALOG_MESSAGE, resultMsg);
            msg.setData(b);
            mHandler.sendMessage(msg);
        } else {
            Message msg = new Message();
            msg.what = Constant.TRANSACTION_SUCCESSED;
            Bundle b = new Bundle();
            b.putString(Constant.DIALOG_TITLE, "SUCCESS");
            b.putString(Constant.DIALOG_MESSAGE, mGetIpFromWeb.getResponseVar("ResultMsg"));
            b.putString(getResources().getString(R.string.commSetting_destIP), mGetIpFromWeb.getResponseVar("IPaddress"));
            b.putString(getResources().getString(R.string.commSetting_destPort), mGetIpFromWeb.getResponseVar("Port"));
            b.putString(getResources().getString(R.string.commSetting_mac), mGetIpFromWeb.getResponseVar("MacAddress"));
            msg.setData(b);
            mHandler.sendMessage(msg);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (Convenience.isButtonClickEnough()) {
            setHostContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public Dialog createDialog() {
        return ProcessProgressDialog.createDialog(getActivity(), "Getting IP...",false, true, this);
    }
}

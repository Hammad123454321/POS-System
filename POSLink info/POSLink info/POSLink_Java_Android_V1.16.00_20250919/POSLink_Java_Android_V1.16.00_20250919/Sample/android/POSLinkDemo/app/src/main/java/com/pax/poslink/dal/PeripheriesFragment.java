package com.pax.poslink.dal;

import static android.app.Activity.RESULT_OK;
import static com.pax.poslink.peripheries.ProcessResult.CODE_MSG_MAP;
import static com.pax.poslink.peripheries.ProcessResult.MESSAGE_NO_SUPPORT_ERROR;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pax.poslink.BuildConfig;
import com.pax.poslink.R;
import com.pax.poslink.business.ActivityResultReceiver;
import com.pax.poslink.business.UIBusiness;
import com.pax.poslink.dal.baseSystem.BaseSystemActivity;
import com.pax.poslink.dal.icc.IccActivity;
import com.pax.poslink.dal.m1card.MifareActivity;
import com.pax.poslink.dal.modem.ModemActivity;
import com.pax.poslink.dal.print.PrintBitmapDialog;
import com.pax.poslink.dal.print.PrintStringDialog;
import com.pax.poslink.dal.scan.ScanActivity;
import com.pax.poslink.dal.scan.ScanHwConfigActivity;
import com.pax.poslink.dal.scan.ScanResultModeActivity;
import com.pax.poslink.dal.serialport.SerialPortActivity;
import com.pax.poslink.entity.LanParam;
import com.pax.poslink.entity.ScanResult;
import com.pax.poslink.entity.WifiParam;
import com.pax.poslink.exceptions.PaxScannerHwException;
import com.pax.poslink.peripheries.DeviceModel;
import com.pax.poslink.peripheries.MiscSettings;
import com.pax.poslink.peripheries.POSLinkBluetoothPrinter;
import com.pax.poslink.peripheries.POSLinkCashDrawer;
import com.pax.poslink.peripheries.POSLinkPrinter;
import com.pax.poslink.peripheries.POSLinkScanner;
import com.pax.poslink.peripheries.POSLinkScannerHw;
import com.pax.poslink.peripheries.ProcessResult;
import com.pax.poslink.ui.FileManagerActivity;
import com.pax.poslink.ui.base.BaseFragment;
import com.pax.poslink.util.BlueToothHelper;
import com.pax.poslink.util.Constant;
import com.pax.poslink.util.FileUtils;
import com.pax.poslink.util.LogStaticWrapper;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.util.thread.AppThreadPool;
import com.pax.poslink.view.ExtDataEntity;
import com.pax.poslink.view.NameValueBrowserEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;
import com.pax.poslink.view.SingleButtonEntity;
import com.pax.poslink.widget.ProcessProgressDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Leon.F on 2018/3/22.
 */
public class PeripheriesFragment extends Fragment {

    private POSLinkScanner posLinkScanner;
    private static final String TAG = "PeripheriesFragment";
    private View rootView;
    private POSLinkScannerHw posLinkScannerHw;
    protected ActivityResultReceiver activityResultReceiver;

    private final int REQUEST_BT_ENABLE = 1;
    private String statu = "";
    private String bootAnimationFilePath = "";
    private String bootLogoFilePath = "";
    private List<RenderEntity> requestRenderEntityList = new ArrayList<>();
    private ViewGroup requestContainer;

    public static PeripheriesFragment newInstance() {
        Bundle args = new Bundle();

        PeripheriesFragment fragment = new PeripheriesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dal, container, false);
        initView(view);
        this.rootView = view;
        return view;
    }

    private void initView(final View view) {
        initPrinter(view);

        initScanner(view);

        initCashDrawer(view);

        initMiscSettings(view);

        initBaseSystem(view);

        initModem(view);

        initSerialPort(view);

        initIcc(view);

        initMIFARE(view);
        LogStaticWrapper.getLog().v("SN:" + DeviceModel.getSN(getContext()));
    }


    private void initPrinter(final View view) {
        final ViewGroup printerParamsContainer = (ViewGroup) view.findViewById(R.id.printer_params_container);
        final List<RenderEntity> selectItems = new ArrayList<RenderEntity>();
        final List<CommonItemView> commonItemViews = new ArrayList<>();

        List<String> valueList = Arrays.asList(
                String.valueOf(POSLinkPrinter.CutMode.DO_NOT_CUT),
                String.valueOf(POSLinkPrinter.CutMode.FULL_PAPER_CUT),
                String.valueOf(POSLinkPrinter.CutMode.PARTIAL_PAPER_CUT)
        );

        final ExtDataEntity buttonEntity = new ExtDataEntity("MACADDRESS", "", InputType.TYPE_CLASS_TEXT, "", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, final NameValueStringEntity entity) {
//                POSLinkBluetoothPrinter.getInstance(getContext()).disconnectPrinter();
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String mac) {
                        entity.setValue(mac);
                        commonItemViews.get(6).render(entity);
                    }
                });
                if (BluetoothAdapter.getDefaultAdapter() == null) {
                    UIUtil.showToast(getContext(), "Bluetooth is not available!", Toast.LENGTH_SHORT);
                } else {
                    if (BlueToothHelper.checkPermission(getActivity())) {
                        initBlueToothPrint();
                    }
                }
            }
        });
        final NameValueSelectEntity printTypeEntity = new NameValueSelectEntity("PrintType", "0", Arrays.asList("Printer(Own)", "BlueTooth Printer(eg.BP60A)"),
                Arrays.asList("0", "1"), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, String selectedValue, int position) {
            }
        });

        final NameValueSelectEntity cutModeEntity = new NameValueSelectEntity("Cut mode", valueList.get(0), Arrays.asList("DO_NOT_CUT", "0:FullPaper", "1:PARTIAL_PAPER_CUT"), valueList, 0);
        final NameValueSelectEntity typefaceEntity = new NameValueSelectEntity("Typeface", "0", Arrays.asList("DEFAULT", "MONOSPACE"), Arrays.asList("0", "1"), 0);

        final NameValueStringEntity greyEntity = new NameValueStringEntity("Grey", "", InputType.TYPE_CLASS_NUMBER,
                String.format(Locale.ENGLISH, "%d: Default; %d: TWO_LAYER_THERMAL; %d: HIGHER_TWO_LAYER_THERMAL; %d-%d: LOWEST_PERCENTAGE - HIGHEST_PERCENTAGE",
                        POSLinkPrinter.GreyLevel.DEFAULT,
                        POSLinkPrinter.GreyLevel.TWO_LAYER_THERMAL,
                        POSLinkPrinter.GreyLevel.HIGHER_TWO_LAYER_THERMAL,
                        POSLinkPrinter.GreyLevel.LOWEST_PERCENTAGE,
                        POSLinkPrinter.GreyLevel.HIGHEST_PERCENTAGE
                ));
        Integer recommendWidth = POSLinkPrinter.RecommendWidth.MODEL_MAP_RECOMMEND_WIDTH.get(Build.MODEL);
        recommendWidth = recommendWidth == null ? POSLinkPrinter.RecommendWidth.E500_RECOMMEND_WIDTH : recommendWidth;
        final NameValueStringEntity printWidthEntity = new NameValueStringEntity("PrintWidth", String.valueOf(recommendWidth), InputType.TYPE_CLASS_NUMBER, "");
        final NameValueStringEntity printStatusEntity = new NameValueStringEntity("PrinterStatus", statu, InputType.TYPE_CLASS_NUMBER, "");
        AppThreadPool.getInstance().runInOtherThread(new Runnable() {
            @Override
            public void run() {
                int status = POSLinkPrinter.getInstance(getContext()).getStatus();
                statu = POSLinkPrinter.STATU_CODE_MSG_MAP.get(status);
                AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        printStatusEntity.setValue(statu);
                        commonItemViews.get(5).render(printStatusEntity);
                    }
                });
            }
        });
        selectItems.add(printTypeEntity);
        selectItems.add(cutModeEntity);
        selectItems.add(typefaceEntity);
        selectItems.add(greyEntity);
        selectItems.add(printWidthEntity);
        selectItems.add(printStatusEntity);
        selectItems.add(buttonEntity);

        printerParamsContainer.removeAllViews();
        for (RenderEntity nameValueEntity : selectItems) {
            CommonItemView itemView = nameValueEntity.createView(printerParamsContainer);
            commonItemViews.add(itemView);
            itemView.render(nameValueEntity);
            printerParamsContainer.addView(itemView.getView());
        }

        final View printBtn = view.findViewById(R.id.print_btn);
        final View cutBtn = view.findViewById(R.id.cut_paper);
        cutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cutMode = Integer.parseInt(cutModeEntity.getItemValues().get(cutModeEntity.getSelectedItem()));
                if (cutMode == POSLinkPrinter.CutMode.DO_NOT_CUT) {
                    return;
                }
                if (printTypeEntity.getSelectedItem() == 0) {
                    int code = POSLinkPrinter.getInstance(v.getContext()).cutPaper(cutMode);
                    if (code == 0) {
                        UIUtil.showToast(v.getContext(), "Cut paper success", Toast.LENGTH_SHORT);
                    } else {
                        String errorCode = "Cut paper error";
                        if (POSLinkPrinter.getInstance(v.getContext()).ERROR_CODE_MSG_MAP.containsKey(code)) {
                            errorCode = POSLinkPrinter.getInstance(v.getContext()).ERROR_CODE_MSG_MAP.get(code);
                            errorCode = CODE_MSG_MAP.get(errorCode);
                        }
                        UIUtil.showToast(v.getContext(), errorCode, Toast.LENGTH_SHORT);
                    }
                } else {
                    setPrintParam(v.getContext(), greyEntity, printWidthEntity, buttonEntity, printTypeEntity.getSelectedItem(), typefaceEntity.getSelectedItem());
                    POSLinkBluetoothPrinter.getInstance(v.getContext()).feedPaper(175);
                    int ret = POSLinkBluetoothPrinter.getInstance(v.getContext()).cutPaper(cutMode);
                    if (ret == 0) {
                        UIUtil.showToast(v.getContext(), "Cut paper success", Toast.LENGTH_SHORT);
                    } else {
                        String errorCode = "Cut paper error";
                        UIUtil.showToast(v.getContext(), errorCode, Toast.LENGTH_SHORT);
                    }
                }
            }
        });
        printBtn.setOnClickListener(new View.OnClickListener() {

            private PrintBitmapDialog dialog;

            @Override
            public void onClick(View v) {
                setPrintParam(v.getContext(), greyEntity, printWidthEntity, buttonEntity, printTypeEntity.getSelectedItem(), typefaceEntity.getSelectedItem());
                if (dialog == null) {
                    dialog = new PrintBitmapDialog(v.getContext());
                }
                int cutMode = Integer.parseInt(cutModeEntity.getItemValues().get(cutModeEntity.getSelectedItem()));
                dialog.setMacAdd(buttonEntity.getValue());
                dialog.show(cutMode, printTypeEntity.getSelectedItem());

            }
        });

        final View printStrBtn = view.findViewById(R.id.print_data_btn);
        printStrBtn.setOnClickListener(new View.OnClickListener() {

            private PrintStringDialog dialog;

            @Override
            public void onClick(View v) {
                setPrintParam(v.getContext(), greyEntity, printWidthEntity, buttonEntity, printTypeEntity.getSelectedItem(), typefaceEntity.getSelectedItem());
                if (dialog == null) {
                    dialog = new PrintStringDialog(v.getContext());
                }
                int cutMode = Integer.parseInt(cutModeEntity.getItemValues().get(cutModeEntity.getSelectedItem()));
                dialog.setMacAdd(buttonEntity.getValue());
                dialog.show(cutMode, printTypeEntity.getSelectedItem());
            }
        });
    }

    private static void setPrintParam(Context context, NameValueStringEntity greyEntity,
                                      NameValueStringEntity prnWidthEntity, ExtDataEntity macEntity, int type,
                                      int typeface) {
        if (type == 0) {
            POSLinkPrinter posLinkPrinter = POSLinkPrinter.getInstance(context);
            posLinkPrinter.setGray(TextUtils.isEmpty(greyEntity.getValue()) ? POSLinkPrinter.GreyLevel.DEFAULT : Integer.parseInt(greyEntity.getValue()));
            posLinkPrinter.setPrintWidth(StringUtil.parseInt(prnWidthEntity.getValue()));
            posLinkPrinter.setTypeFace(typeface == 0 ? Typeface.DEFAULT : Typeface.MONOSPACE);
        } else {
            POSLinkBluetoothPrinter posLinkBluetoothPrinter = POSLinkBluetoothPrinter.getInstance(context);
            posLinkBluetoothPrinter.setMacAddress(macEntity.getValue());
            posLinkBluetoothPrinter.setGray(TextUtils.isEmpty(greyEntity.getValue()) ? POSLinkPrinter.GreyLevel.DEFAULT : Integer.parseInt(greyEntity.getValue()));
            posLinkBluetoothPrinter.setTypeFace(typeface == 0 ? Typeface.DEFAULT : Typeface.MONOSPACE);
        }

    }

    private void initScanner(View view) {
        List<String> list = Arrays.asList(
                POSLinkScanner.ScannerType.FRONT,
                POSLinkScanner.ScannerType.REAR,
                POSLinkScanner.ScannerType.LASER
        );
        posLinkScannerHw = POSLinkScannerHw.getInstance(getActivity());
        final NameValueSelectEntity scannerModeEntity = new NameValueSelectEntity("Scanner Type", list.get(0), list, list, 0);
        final List<RenderEntity> selectItems = new ArrayList<RenderEntity>() {
            {
                add(scannerModeEntity);
            }
        };
        ViewGroup scannerParamsContainer = (ViewGroup) view.findViewById(R.id.scanner_params_container);
        scannerParamsContainer.removeAllViews();
        for (RenderEntity nameValueEntity : selectItems) {
            CommonItemView itemView = nameValueEntity.createView(scannerParamsContainer);
            itemView.render(nameValueEntity);
            scannerParamsContainer.addView(itemView.getView());
        }

        final TextView scannerResultTxt = (TextView) view.findViewById(R.id.txt_scanner_result);
        View scannerOpen = view.findViewById(R.id.scanner_open);
        scannerOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        posLinkScanner = POSLinkScanner.getPOSLinkScanner(v.getContext(), scannerModeEntity.getItemValues().get(scannerModeEntity.getSelectedItem()));
                        final ProcessResult result = posLinkScanner.open();
                        if (!result.getCode().equals(ProcessResult.CODE_OK)) {
                            AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showErrorMsg(v.getContext(), result);
                                }
                            });
                        }
                    }
                });

            }
        });

        View scannerStart = view.findViewById(R.id.scanner_start);
        scannerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                scannerResultTxt.setText("");
                if (posLinkScanner == null) {
                    Toast.makeText(v.getContext(), "You have not opened scanner", Toast.LENGTH_SHORT).show();
                    return;
                }
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        posLinkScanner.start(new POSLinkScanner.IScanListener() {
                            @Override
                            public void onRead(final ScanResult result) {
                                AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scannerResultTxt.setText(scannerResultTxt.getText() + " Format:" + result.getFormat() + ", content:" + result.getContent());
                                    }
                                });
                            }

                            @Override
                            public void onFinish() {
                                AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(v.getContext(), "Finish", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(String s) {
                                LogStaticWrapper.getLog().e(s);
                                AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(v.getContext(), MESSAGE_NO_SUPPORT_ERROR, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });
        View scannerClose = view.findViewById(R.id.scanner_close);
        scannerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (posLinkScanner == null) {
                    Toast.makeText(v.getContext(), "You have not opened scanner", Toast.LENGTH_SHORT).show();
                    return;
                }
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        final ProcessResult result = posLinkScanner.close();
                        if (!result.getCode().equals(ProcessResult.CODE_OK)) {
                            AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showErrorMsg(v.getContext(), result);
                                }
                            });
                        }
                    }
                });

            }
        });

        view.findViewById(R.id.scan_decoder_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanActivity.start(v.getContext());
            }
        });
        view.findViewById(R.id.scanhw_config_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanHwConfigActivity.start(v.getContext());
            }
        });

        view.findViewById(R.id.scan_set_mode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanResultModeActivity.start(getContext());
            }
        });

        View scanHw = view.findViewById(R.id.scanhw_btn);
        scanHw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            posLinkScannerHw.scanOpen();
                            final ScanResult decodeResult = posLinkScannerHw.scanRead(10000);
                            final StringBuilder resultScan = new StringBuilder();
                            resultScan.append("Format:").append(decodeResult.getFormat()).append("\n")
                                    .append("Content:").append(decodeResult.getContent()).append("\n");
                            AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scannerResultTxt.setText(resultScan.toString());
                                }
                            });
                            posLinkScannerHw.scanClose();
                        } catch (PaxScannerHwException e) {
                            UIUtil.showToast(getActivity(), e.getErrMsg(), Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });
    }

    private void initCashDrawer(View view) {
        final View cashDrawerBtn = view.findViewById(R.id.cashdrawer_btn);
        cashDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        ProcessResult result = POSLinkCashDrawer.getInstance(v.getContext()).open();
                        if (!result.getCode().equals(ProcessResult.CODE_OK)) {
                            UIUtil.showToast(getActivity(), result.getMessage(), Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        });


        final View cashStatusBtn = view.findViewById(R.id.cashdrawer_status_btn);
        cashStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        int result = POSLinkCashDrawer.getInstance(v.getContext()).cashBoxStatus();
                        String message;
                        if (result < 0) {
                            message = "Not Support";
                        } else {
                            message = result == 1 ? "Open" : "Close";
                        }
                        UIUtil.showToast(getActivity(), message, Toast.LENGTH_SHORT);
                    }
                });
            }
        });
    }

    private void initMiscSettings(View view) {
        final List<CommonItemView> commonItemViews = new ArrayList<>();

        requestContainer = (LinearLayout) view.findViewById(R.id.settings_params_container);

        String enable = "Enabled";
        String disable = "Disabled";
        String visible = "Visible";
        String invisible = "Invisible";
        String isTrue = "true";
        String isFalse = "false";
        String charge_only = "CHARGE_ONLY";
        String defaultValue = "DEFAULT";
        final List<String> list = new ArrayList<>();
        for (MiscSettings.ScreenOffTime type : MiscSettings.ScreenOffTime.values()) {
            list.add(type.name());
        }

        final NameValueSelectEntity homeSelectEntity = new NameValueSelectEntity("HomeKey",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setHomeKeyEnable(getContext(), Boolean.valueOf(selectedValue));
                    }
                });
            }
        });

        final NameValueSelectEntity recentSelectEntity = new NameValueSelectEntity("RecentKey",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setRecentKeyEnable(getContext(), Boolean.valueOf(selectedValue));
                    }
                });
            }
        });

        final NameValueSelectEntity statusBarEnableSelectEntity = new NameValueSelectEntity("StatusBar",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setStatusBarEnable(getContext(), Boolean.valueOf(selectedValue));
                    }
                });

            }
        });

        final NameValueSelectEntity statusBarVisibleSelectEntity = new NameValueSelectEntity("StatusBar",
                "true",
                Arrays.asList(visible, invisible),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setStatusBarVisible(getContext(), Boolean.valueOf(selectedValue));
                    }
                });
            }
        });

        final NameValueSelectEntity navigationBarEnableSelectEntity = new NameValueSelectEntity("NavigationBar",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setNavigationBarEnable(getContext(), Boolean.valueOf(selectedValue));
                    }
                });

            }
        });

        final NameValueSelectEntity navigationBarVisibleSelectEntity = new NameValueSelectEntity("NavigationBar",
                "true",
                Arrays.asList(visible, invisible),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setNavigationBarVisible(getContext(), Boolean.valueOf(selectedValue));
                    }
                });

            }
        });

        final NameValueSelectEntity wifiHotspotEnableSelectEntity = new NameValueSelectEntity("WifiHotspot",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)),
                0,
                new NameValueSelectEntity.OnSelectCallback() {
                    @Override
                    public void onSelect(View view, final String selectedValue, int position) {
                        AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                            @Override
                            public void run() {
                                MiscSettings.setWifiHotspotSettingsEnable(getContext(), Boolean.valueOf(selectedValue));
                            }
                        });

                    }
                });

        final NameValueSelectEntity powerKeyEnableSelectEntity = new NameValueSelectEntity("PowerKey",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)),
                0,
                new NameValueSelectEntity.OnSelectCallback() {
                    @Override
                    public void onSelect(View view, final String selectedValue, int position) {
                        AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                            @Override
                            public void run() {
                                MiscSettings.setPowerKeyEnable(getContext(), Boolean.valueOf(selectedValue));
                            }
                        });

                    }
                });

        final SingleButtonEntity disablePOSMenuEntity = new SingleButtonEntity("DISABLEPOSMENU", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                DisablePOSMenuActivity.start(getActivity());
            }
        });

        final NameValueSelectEntity physicalKeyEnableSelectEntity = new NameValueSelectEntity("PhysicalKey",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setPhysicalKeyEnable(getContext(), Boolean.valueOf(selectedValue));
                    }
                });

            }
        });
        final NameValueSelectEntity screenTimeoutSelectEntity = new NameValueSelectEntity("ScreenOffTime",
                list.get(0),
                list,
                list, list.size() - 1, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setScreenOffTime(getContext(), MiscSettings.ScreenOffTime.valueOf(selectedValue));
                    }
                });

            }
        });
        final NameValueSelectEntity cellularDataEnableSelectEntity = new NameValueSelectEntity("MobileData",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setMobileDataEnable(getContext(), Boolean.valueOf(selectedValue));
                    }
                });

            }
        });
        final NameValueSelectEntity usbModeSelectEntity = new NameValueSelectEntity("USB Mode",
                "DEFAULT",
                Arrays.asList(charge_only, defaultValue),
                Arrays.asList("0", "1"), 1, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setUsbMode(getContext(), position);
                    }
                });

            }
        });

        final SingleButtonEntity addApnsEntity = new SingleButtonEntity("ADDAPNS", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                byte[] apns = FileUtils.getFromRaw(getContext());
                boolean succeed = MiscSettings.addApns(getContext(), apns);
                if (!succeed) {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        final SingleButtonEntity getApnListEntity = new SingleButtonEntity("getApnList", new SingleButtonEntity.ClickCallback() {
//            @Override
//            public void onClick(final View v, SingleButtonEntity renderEntity) {
//                List<ApnInfo> apnInfos = MiscSettings.getApnList(getContext());
//                if (apnInfos != null) {
//                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        final SingleButtonEntity getCurrentApnEntity = new SingleButtonEntity("getCurrentApn", new SingleButtonEntity.ClickCallback() {
//            @Override
//            public void onClick(final View v, SingleButtonEntity renderEntity) {
//                ApnInfo apnInfo = MiscSettings.getCurrentApn(getContext());
//                if (apnInfo != null) {
//                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        final SingleButtonEntity removeApnEntity = new SingleButtonEntity("removeApn", new SingleButtonEntity.ClickCallback() {
//            @Override
//            public void onClick(final View v, SingleButtonEntity renderEntity) {
//                boolean isSuccess = MiscSettings.removeApn(getContext(), "1");
//                if (!isSuccess) {
//                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        final SingleButtonEntity switchAPNEntity = new SingleButtonEntity("switchAPN", new SingleButtonEntity.ClickCallback() {
//            @Override
//            public void onClick(final View v, SingleButtonEntity renderEntity) {
//                boolean isSuccess = MiscSettings.switchAPN(getContext(), "net", "user", "123123");
//                if (!isSuccess) {
//                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        final SingleButtonEntity switchAPNByNameEntity = new SingleButtonEntity("switchAPNByName", new SingleButtonEntity.ClickCallback() {
//            @Override
//            public void onClick(final View v, SingleButtonEntity renderEntity) {
//                boolean isSuccess = MiscSettings.switchAPN(getContext(), "apn", "nettt", "user", "123123", 3);
//                if (!isSuccess) {
//                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        final NameValueStringEntity localIpEntity = new NameValueStringEntity("Local IP", "", InputType.TYPE_CLASS_TEXT, "local ip");
        final NameValueStringEntity subnetMaskEntity = new NameValueStringEntity("SubNetMask", "", InputType.TYPE_CLASS_TEXT, "netmask");
        final NameValueStringEntity gatewayEntity = new NameValueStringEntity("Gateway", "", InputType.TYPE_CLASS_TEXT, "gateway");
        final NameValueStringEntity dns1Entity = new NameValueStringEntity("DNS1", "", InputType.TYPE_CLASS_TEXT, "DSN1");
        final NameValueStringEntity dns2Entity = new NameValueStringEntity("DNS2", "", InputType.TYPE_CLASS_TEXT, "DSN2");
        final NameValueSelectEntity dhcpEntity = new NameValueSelectEntity("DHCP",
                isTrue,
                Arrays.asList(isTrue, isFalse),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, String selectedValue, int position) {

            }
        });
        final NameValueStringEntity networkPrefixLengthEntity = new NameValueStringEntity("NetworkPrefixLength", "", InputType.TYPE_CLASS_NUMBER, "networkPrefixLength");
        final NameValueSelectEntity reconnectEntity = new NameValueSelectEntity("Reconnect",
                isFalse,
                Arrays.asList(isTrue, isFalse),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, String selectedValue, int position) {

            }
        });
        final SingleButtonEntity setWifiStaticIpEntity = new SingleButtonEntity("setWifiStaticIp", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {

                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        String ip = localIpEntity.getValue();
                        String subnetMask = subnetMaskEntity.getValue();
                        String gateway = gatewayEntity.getValue();
                        String dns1 = dns1Entity.getValue();
                        String dns2 = dns2Entity.getValue();
                        boolean dhcp = Boolean.valueOf(dhcpEntity.getValue());
                        String networkPrefixLength = networkPrefixLengthEntity.getValue();
                        int length = 0;
                        if (StringUtil.isNumber(networkPrefixLength)) {
                            length = Integer.valueOf(networkPrefixLength);
                        }
                        boolean isReconnect = Boolean.valueOf(reconnectEntity.getValue());
                        WifiParam wifiParam = new WifiParam();
                        wifiParam.setSubnetMask(subnetMask);
                        wifiParam.setDhcp(dhcp);
                        wifiParam.setLocalIp(ip);
                        wifiParam.setDns1(dns1);
                        wifiParam.setGateway(gateway);
                        wifiParam.setDns2(dns2);
                        wifiParam.setNetworkPrefixLength(length);
                        wifiParam.setReconnect(isReconnect);
                        final boolean isSuccess = MiscSettings.setWifiStaticIp(getContext(), wifiParam);
                        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isSuccess) {
                                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });

            }
        });
        final SingleButtonEntity setParamEntity = new SingleButtonEntity("setLanParam", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        String ip = localIpEntity.getValue();
                        String subnetMask = subnetMaskEntity.getValue();
                        String gateway = gatewayEntity.getValue();
                        String dns1 = dns1Entity.getValue();
                        String dns2 = dns2Entity.getValue();
                        boolean dhcp = Boolean.valueOf(dhcpEntity.getValue());
                        LanParam lanParam = new LanParam();
                        lanParam.setLocalIp(ip);
                        lanParam.setDns1(dns1);
                        lanParam.setGateway(gateway);
                        lanParam.setDns2(dns2);
                        lanParam.setDhcp(dhcp);
                        lanParam.setSubnetMask(subnetMask);
                        final boolean isSuccess = MiscSettings.setLanParam(getContext(), lanParam);
                        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isSuccess) {
                                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }
        });

        final NameValueSelectEntity enabledScreenSaverEntity = new NameValueSelectEntity("ScreenSaver",
                "true",
                Arrays.asList(enable, disable),
                Arrays.asList(String.valueOf(true), String.valueOf(false)), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.enabledScreenSaver(getContext(), Boolean.valueOf(selectedValue));
                    }
                });
            }
        });
        final NameValueSelectEntity screenSaverActivateTypeEntity = new NameValueSelectEntity("ScreenSaverActivateType",
                "0",
                Arrays.asList("0", "1", "2"),
                Arrays.asList("0", "1", "2"), 0, new NameValueSelectEntity.OnSelectCallback() {
            @Override
            public void onSelect(View view, final String selectedValue, int position) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        MiscSettings.setScreenSaverActivateType(getContext(), Integer.valueOf(selectedValue));
                    }
                });
            }
        });
        final NameValueStringEntity screenSaverPackageEntity = new NameValueStringEntity("ScreenSaver Package", BuildConfig.APPLICATION_ID, InputType.TYPE_CLASS_TEXT, "screen saver package name");
        final NameValueStringEntity screenSaverClassEntity = new NameValueStringEntity("ScreenSaver Class", "com.pax.poslink.ScreenSaverService", InputType.TYPE_CLASS_TEXT, "screen saver class name");
        final SingleButtonEntity setScreenSaverEntity = new SingleButtonEntity("setScreenSaver", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        String pkgName = screenSaverPackageEntity.getValue();
                        String className = screenSaverClassEntity.getValue();
                        final boolean isSuccess = MiscSettings.setScreenSaver(getContext(), pkgName, className);
                        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isSuccess) {
                                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

            }
        });
        final SingleButtonEntity getScreenSaverEntity = new SingleButtonEntity("getScreenSaver", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        final String screenSaver = MiscSettings.getScreenSaver(getContext());
                        AppThreadPool.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), screenSaver, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });
        final NameValueBrowserEntity bootAnimationFilePathEntity = new NameValueBrowserEntity("Boot Animation File Path", "Browse", bootAnimationFilePath, InputType.TYPE_CLASS_TEXT, "Boot Animation File Path", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        entity.setValue(data);
                        bootAnimationFilePath = data;
                        UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                    }
                });
                String animationFile = entity.getValue();
                File f = new File(animationFile);
                if (!f.exists()) {
                    animationFile = Environment.getExternalStorageDirectory().toString();
                } else {
                    if (f.isFile())
                        animationFile = animationFile.substring(0, animationFile.lastIndexOf("/"));
                }

                Intent intent = new Intent(getActivity(), FileManagerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("UploadImagePath", animationFile);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.MANAGE_FILE_PATH_RESULT);
            }
        });
        final SingleButtonEntity setBootAnimationFilePathEntity = new SingleButtonEntity("Set Boot Animation File Path", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isSuccess = MiscSettings
                                .setBootAnimation(getContext(), bootAnimationFilePath);
                        AppThreadPool.getInstance().runOnUiThread(() -> {
                            if (!isSuccess) {
                                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });
        NameValueBrowserEntity bootLogoFilePathEntity = new NameValueBrowserEntity("Boot Logo File Path", "Browse", bootLogoFilePath, InputType.TYPE_CLASS_TEXT, "Boot Logo File Path", new NameValueStringEntity.ClickCallback() {
            @Override
            public void onClick(View v, NameValueStringEntity entity) {
                setActivityResultReceiver(new ActivityResultReceiver() {
                    @Override
                    public void onReceive(String data) {
                        entity.setValue(data);
                        bootLogoFilePath = data;
                        UIBusiness.notifyDataSetChangeForContainer(requestRenderEntityList, requestContainer);
                    }
                });
                String logoFile = entity.getValue();
                File f = new File(logoFile);
                if (!f.exists()) {
                    logoFile = Environment.getExternalStorageDirectory().toString();
                } else {
                    if (f.isFile())
                        logoFile = logoFile.substring(0, logoFile.lastIndexOf("/"));
                }

                Intent intent = new Intent(getActivity(), FileManagerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("UploadImagePath", logoFile);
                intent.putExtras(bundle);
                startActivityForResult(intent, Constant.MANAGE_FILE_PATH_RESULT);
            }
        });
        final SingleButtonEntity setBootLogoFilePathEntity = new SingleButtonEntity("Set Boot Logo File Path", new SingleButtonEntity.ClickCallback() {
            @Override
            public void onClick(final View v, SingleButtonEntity renderEntity) {
                AppThreadPool.getInstance().runInOtherThread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean isSuccess = MiscSettings
                                .setBootLogo(getContext(), bootLogoFilePath);
                        AppThreadPool.getInstance().runOnUiThread(() -> {
                            if (!isSuccess) {
                                Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
        });

        final List<RenderEntity> selectItems = new ArrayList<RenderEntity>() {
            {
                add(homeSelectEntity);
                add(recentSelectEntity);
                add(statusBarEnableSelectEntity);
                add(statusBarVisibleSelectEntity);
                add(navigationBarEnableSelectEntity);
                add(navigationBarVisibleSelectEntity);
                add(wifiHotspotEnableSelectEntity);
                add(powerKeyEnableSelectEntity);
                add(physicalKeyEnableSelectEntity);
                add(screenTimeoutSelectEntity);
                add(cellularDataEnableSelectEntity);
                add(usbModeSelectEntity);
//                add(switchSimCardEntity);
                add(addApnsEntity);
//                add(getApnListEntity);
//                add(switchAPNEntity);
//                add(switchAPNByNameEntity);
//                add(getCurrentApnEntity);
//                add(removeApnEntity);
                add(disablePOSMenuEntity);

                add(localIpEntity);
                add(subnetMaskEntity);
                add(gatewayEntity);
                add(dns1Entity);
                add(dns2Entity);
                add(dhcpEntity);
                add(networkPrefixLengthEntity);
                add(reconnectEntity);
                add(setWifiStaticIpEntity);
                add(setParamEntity);
                add(enabledScreenSaverEntity);
                add(screenSaverActivateTypeEntity);
                add(screenSaverPackageEntity);
                add(screenSaverClassEntity);
                add(setScreenSaverEntity);
                add(getScreenSaverEntity);
                add(bootAnimationFilePathEntity);
                add(setBootAnimationFilePathEntity);
                add(bootLogoFilePathEntity);
                add(setBootLogoFilePathEntity);
            }
        };

        Button switchCard1 = view.findViewById(R.id.sim_card_1);
        switchCard1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSimCard(1);
            }
        });

        Button switchCard2 = view.findViewById(R.id.sim_card_2);
        switchCard2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchSimCard(2);
            }
        });
        requestContainer.removeAllViews();
        requestRenderEntityList.clear();
        if (selectItems != null) {
            requestRenderEntityList.addAll(selectItems);
        }
        for (RenderEntity nameValueEntity : requestRenderEntityList) {
            CommonItemView itemView = nameValueEntity.createView(requestContainer);
            itemView.getView().setTag(itemView);
            itemView.render(nameValueEntity);
            requestContainer.addView(itemView.getView());
        }
    }

    private void showErrorMsg(Context context, ProcessResult result) {
        Toast.makeText(context, result.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void initModem(View view) {
        view.findViewById(R.id.btn_modem_sample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ModemActivity.start(v.getContext());
            }
        });
    }


    private void initSerialPort(View view) {
        view.findViewById(R.id.btn_serial_port_sample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerialPortActivity.start(v.getContext());
            }
        });
    }

    private void initIcc(View view) {
        view.findViewById(R.id.btn_icc_sample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IccActivity.start(v.getContext());
            }
        });
    }

    private void initBaseSystem(View view) {
        view.findViewById(R.id.btn_base_sys_sample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseSystemActivity.start(v.getContext());
            }
        });
    }

    private void initMIFARE(View view) {
        view.findViewById(R.id.btn_m1_sample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MifareActivity.start(v.getContext());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
//        initMiscSettings(rootView);
    }


    private void initBlueToothPrint() {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        Intent intent;
        if (!btAdapter.isEnabled()) {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_BT_ENABLE);
        } else {
            BlueToothHelper.initBlueTooth(getContext(), new BlueToothHelper.BlueCallback() {
                @Override
                public void onFinish(String macAddress) {
                    if (activityResultReceiver != null)
                        activityResultReceiver.onReceive(macAddress);
                }
            });
        }
    }

    protected void setActivityResultReceiver(ActivityResultReceiver activityResultReceiver) {
        this.activityResultReceiver = activityResultReceiver;
    }

    private void switchSimCard(final int slot) {
        Single<Boolean> single = Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@io.reactivex.annotations.NonNull SingleEmitter<Boolean> emitter) throws Exception {
                boolean isSuccess = MiscSettings.switchSimCard(getContext(), slot);
                emitter.onSuccess(isSuccess);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        single.subscribe(new SingleObserver<Boolean>() {
            final Dialog processingDialog = ProcessProgressDialog.createDialog(getContext(), getContext().getString(R.string.processing), false, false,
                    new ProcessProgressDialog.OnSetListener() {
                        @Override
                        public void onSetListener(ProgressDialog dialog, boolean cancelable, boolean enDismiss) {
                        }
                    });

            @Override
            public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                processingDialog.show();
            }

            @Override
            public void onSuccess(@io.reactivex.annotations.NonNull Boolean aBoolean) {
                processingDialog.dismiss();
                if (Boolean.FALSE.equals(aBoolean)) {
                    Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Succeed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                processingDialog.dismiss();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_BT_ENABLE:
                    BlueToothHelper.initBlueTooth(getContext(), new BlueToothHelper.BlueCallback() {
                        @Override
                        public void onFinish(String macAddress) {
                            if (activityResultReceiver != null)
                                activityResultReceiver.onReceive(macAddress);
                        }
                    });
                    break;
                case Constant.MANAGE_FILE_PATH_RESULT:
                    if (activityResultReceiver != null)
                        activityResultReceiver.onReceive(data.getStringExtra("UploadImagePath"));
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initBlueToothPrint();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

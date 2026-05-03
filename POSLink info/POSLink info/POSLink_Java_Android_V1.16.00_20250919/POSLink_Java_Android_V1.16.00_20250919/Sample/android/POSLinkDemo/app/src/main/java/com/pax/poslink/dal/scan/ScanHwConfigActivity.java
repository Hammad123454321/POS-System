package com.pax.poslink.dal.scan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.exceptions.PaxScannerHwException;
import com.pax.poslink.peripheries.POSLinkScannerHw;
import com.pax.poslink.peripheries.ScanCodeFormat;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.util.StringUtil;
import com.pax.poslink.util.adapter.CommonItemView;
import com.pax.poslink.util.adapter.RenderEntity;
import com.pax.poslink.view.NameValueSelectEntity;
import com.pax.poslink.view.NameValueStringEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScanHwConfigActivity extends BaseActivity {
    private static final String TAG = "ScanHwConfigActivity";
    private POSLinkScannerHw posLinkScannerHw;
    private List<RenderEntity> barcodeEntityList = new ArrayList<>();
    private List<RenderEntity> settingsItems = new ArrayList<>();

    public static void start(Context context) {
        Intent starter = new Intent(context, ScanHwConfigActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanhw_config);
        initList();
    }

    private void initList() {
        final String open = "Open";
        final String close = "Close";
        final String normal = "Normal";
        final String automatic = "Continuous scan(automatic)";
        final String manual = "Continuous scan(manual)";
        final String vibrate = "vibrate";
        final String sound = "sound";
        final String scanMode = "scan_mode";
        final String scanTimeInterval = "scan_time_interval";
        final String timeIntervalCustom = "time_interval_custom";
        List<String> formatList = Arrays.asList(
                ScanCodeFormat.AZTEC,
                ScanCodeFormat.C11,
                ScanCodeFormat.CODE_128,
                ScanCodeFormat.CODE39,
                ScanCodeFormat.CODE93,
                ScanCodeFormat.CHINAPOST,
                ScanCodeFormat.CODABAR,
                ScanCodeFormat.CODEBLOCK_F,
                ScanCodeFormat.DATA_MATRIX,
                ScanCodeFormat.EAN_8,
                ScanCodeFormat.EAN_13,
                ScanCodeFormat.GS1_DATABAR,
                ScanCodeFormat.HAXIN,
                ScanCodeFormat.CODE_2TO5_INTERLEAVED,
                ScanCodeFormat.MATRIX_25,
                ScanCodeFormat.MAXICODE,
                ScanCodeFormat.MICROPDF,
                ScanCodeFormat.MSI,
                ScanCodeFormat.NEC25,
                ScanCodeFormat.PDF417,
                ScanCodeFormat.QR_CODE,
                ScanCodeFormat.STRAIGHT_25,
                ScanCodeFormat.TELEPEN,
                ScanCodeFormat.TRIOPTIC,
                ScanCodeFormat.UPC_A,
                ScanCodeFormat.UPC_E,
                ScanCodeFormat.US_POSTAL
        );
        List<String> vibrateValueList = Arrays.asList("true", "false");
        List<String> scanValueList = Arrays.asList("0", "1", "2");
        posLinkScannerHw = POSLinkScannerHw.getInstance(this);
        Map<String, Integer> barcodeMap = new HashMap<>();
        try {
            barcodeMap = posLinkScannerHw.getBarcodeMap();
        } catch (PaxScannerHwException e) {
            e.printStackTrace();
        }
        for (String name : formatList) {
            int selectedItem = 0;
            if (barcodeMap != null && barcodeMap.containsKey(name)) {
                selectedItem = barcodeMap.get(name);
            }
            NameValueSelectEntity entity = new NameValueSelectEntity(name, "", Arrays.asList(close, open), Arrays.asList("0", "1"), selectedItem);
            barcodeEntityList.add(entity);
        }

        Map<String, String> settingsMap = new HashMap<>();
        try {
            settingsMap = posLinkScannerHw.getScannerSettings();
            if (settingsMap == null){
                settingsMap = new HashMap<>();
            }
        } catch (PaxScannerHwException e) {
            e.printStackTrace();
        }
        final NameValueSelectEntity vibrateSelectEntity = new NameValueSelectEntity(vibrate,
                "",
                Arrays.asList(open, close),
                vibrateValueList, settingsMap.containsKey(vibrate) ? vibrateValueList.indexOf(settingsMap.get(vibrate)) : 0);
        final NameValueSelectEntity soundSelectEntity = new NameValueSelectEntity(sound,
                "",
                Arrays.asList(open, close),
                vibrateValueList, settingsMap.containsKey(sound) ? vibrateValueList.indexOf(settingsMap.get(sound)) : 0);
        final NameValueSelectEntity scanModeSelectEntity = new NameValueSelectEntity(scanMode,
                "",
                Arrays.asList(normal, automatic, manual),
                scanValueList, settingsMap.containsKey(scanMode) ? scanValueList.indexOf(settingsMap.get(scanMode)) : 0);
        final NameValueSelectEntity scanTimeIntervalSelectEntity = new NameValueSelectEntity(scanTimeInterval,
                "",
                Arrays.asList("0.5s", "1s", "Custom"),
                scanValueList, settingsMap.containsKey(scanTimeInterval) ? scanValueList.indexOf(settingsMap.get(scanTimeInterval)) : 0);
        final NameValueStringEntity timeIntervalCustomEntity = new NameValueStringEntity(timeIntervalCustom, settingsMap != null ? settingsMap.get(timeIntervalCustom) : "", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,
                "Range:0.0~30.0s");

        settingsItems = new ArrayList<RenderEntity>() {
            {

                add(vibrateSelectEntity);
                add(soundSelectEntity);
                add(scanModeSelectEntity);
                add(scanTimeIntervalSelectEntity);
                add(timeIntervalCustomEntity);
            }
        };
        updateView();
    }

    private void updateView() {
        ViewGroup scannerBarcodeContainer = (ViewGroup) findViewById(R.id.scanner_barcode);
        scannerBarcodeContainer.removeAllViews();
        for (RenderEntity nameValueEntity : barcodeEntityList) {
            CommonItemView itemView = nameValueEntity.createView(scannerBarcodeContainer);
            itemView.render(nameValueEntity);
            scannerBarcodeContainer.addView(itemView.getView());
        }
        ViewGroup scannerSettingsContainer = (ViewGroup) findViewById(R.id.scanner_settings);
        scannerSettingsContainer.removeAllViews();
        for (RenderEntity nameValueEntity : settingsItems) {
            CommonItemView itemView = nameValueEntity.createView(scannerSettingsContainer);
            itemView.render(nameValueEntity);
            scannerSettingsContainer.addView(itemView.getView());
        }

        View setBarcodeMap = findViewById(R.id.setBarcodeMap);
        setBarcodeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Integer> map = new HashMap<>();
                for (RenderEntity item : barcodeEntityList) {
                    if (item instanceof NameValueSelectEntity) {
                        NameValueSelectEntity entity = (NameValueSelectEntity) item;
                        map.put(entity.getName(), StringUtil.parseInt(entity.getValue()));
                    }
                }
                try {
                    posLinkScannerHw.setBarcodeMap(map);
                } catch (PaxScannerHwException e) {
                    e.printStackTrace();
                }
            }
        });
        View getBarcodeMap = findViewById(R.id.getBarcodeMap);
        getBarcodeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Map<String, Integer> map = posLinkScannerHw.getBarcodeMap();
                    if (map != null) {
                        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, Integer> entry = it.next();

                            System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
                        }
                    }
                } catch (PaxScannerHwException e) {
                    e.printStackTrace();
                }
            }
        });

        View setScannerSettings = findViewById(R.id.setScannerSettings);
        setScannerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> map = new HashMap<>();
                for (RenderEntity item : settingsItems) {
                    if (item instanceof NameValueSelectEntity) {
                        NameValueSelectEntity entity = (NameValueSelectEntity) item;
                        map.put(entity.getName(), entity.getValue());
                    } else if (item instanceof NameValueStringEntity) {
                        NameValueStringEntity entity = (NameValueStringEntity) item;
                        map.put(entity.getName(), entity.getValue());
                    }
                }
                try {
                    posLinkScannerHw.setScannerSettings(map);
                } catch (PaxScannerHwException e) {
                    e.printStackTrace();
                }
            }
        });
        View getScannerSettings = findViewById(R.id.getScannerSettings);
        getScannerSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Map<String, String> map = posLinkScannerHw.getScannerSettings();
                    if (map != null) {
                        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry<String, String> entry = it.next();
                            System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
                        }
                    }
                } catch (PaxScannerHwException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

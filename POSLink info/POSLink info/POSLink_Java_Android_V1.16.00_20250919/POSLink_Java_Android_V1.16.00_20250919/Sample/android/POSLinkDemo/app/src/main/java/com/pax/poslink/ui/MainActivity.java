
package com.pax.poslink.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pax.gl.commhelper.impl.GLCommDebug;
import com.pax.poslink.ExternalClass;
import com.pax.poslink.MainApplication;
import com.pax.poslink.R;
import com.pax.poslink.broadpos.BroadPOSCommunicator;
import com.pax.poslink.dal.PeripheriesFragment;
import com.pax.poslink.fullIntegration.FullIntegrateFragment;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.internal.TestFragment;
import com.pax.poslink.ui.base.BaseActivity;
import com.pax.poslink.ui.base.TabContainer;
import com.pax.poslink.ui.base.TabItemEntity;
import com.pax.poslink.ui.batch.BatchFragment;
import com.pax.poslink.ui.comsetting.CommSettingFragment;
import com.pax.poslink.ui.formmanage.FormManageFragment;
import com.pax.poslink.ui.logsetting.LogSettingFragment;
import com.pax.poslink.ui.manage.ManageFragment;
import com.pax.poslink.ui.multicmd.MultiCmdFragment;
import com.pax.poslink.ui.pay.PaymentFragment;
import com.pax.poslink.ui.payload.PayloadFragment;
import com.pax.poslink.ui.report.ReportFragment;
import com.pax.poslink.ui.sendcmd.SendCmdFragment;
import com.pax.poslink.util.FragmentCreator;
import com.pax.poslink.util.FragmentsHolder;
import com.pax.poslink.util.LogStaticWrapper;
import com.pax.poslink.util.PermissionUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.adapter.CommonBaseAdapter;
import com.pax.poslink.util.view.DialogUtils;
import com.pax.poslink.util.view.KeyboardUtil;
import com.pax.poslink.view.FloatImageView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1024;
    private List<TabItemEntity> renderEntityList = new ArrayList<>();

    private final Map<Integer, FragmentCreator> fragmentCreatorMap = new HashMap<>();
    private PopupWindow popupWindow;
    private FloatingActionButton floatingActionButton;
    private CommonBaseAdapter<TabItemEntity> adapter;

    {
        fragmentCreatorMap.put(0, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PaymentFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(1, new FragmentCreator() {
            @Override
            public Fragment create() {
                return ManageFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(2, new FragmentCreator() {
            @Override
            public Fragment create() {
                return BatchFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(3, new FragmentCreator() {
            @Override
            public Fragment create() {
                return ReportFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(4, new FragmentCreator() {
            @Override
            public Fragment create() {
                return FormManageFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(5, new FragmentCreator() {
            @Override
            public Fragment create() {
                return MultiCmdFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(6, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PayloadFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(7, new FragmentCreator() {
            @Override
            public Fragment create() {
                return CommSettingFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(8, new FragmentCreator() {
            @Override
            public Fragment create() {
                return LogSettingFragment.newInstance();
            }
        });

        fragmentCreatorMap.put(9, new FragmentCreator() {
            @Override
            public Fragment create() {
                return PeripheriesFragment.newInstance();
            }
        });

        fragmentCreatorMap.put(10, new FragmentCreator() {
            @Override
            public Fragment create() {
                return FullIntegrateFragment.newInstance();
            }
        });
        fragmentCreatorMap.put(11, new FragmentCreator() {
            @Override
            public Fragment create() {
                return SendCmdFragment.newInstance();
            }
        });
        if (Convenience.isButtonClickEnough()) {
            fragmentCreatorMap.put(12, new FragmentCreator() {
                @Override
                public Fragment create() {
                    return TestFragment.Companion.newInstance();
                }
            });
        }

    }

    private FragmentsHolder fragmentsHolder = new FragmentsHolder(fragmentCreatorMap);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poslik_demo);
        askCameraPermission();
        LogStaticWrapper.getLog().d("start demo activity");
        initExternalClass();
        initTab();
        initPopupWindow();
        fragmentsHolder.initFragments(savedInstanceState, getSupportFragmentManager(), R.id.main_fragment_container);
//        fragmentsHolder.setCurrentFragmentIndex(1);
        renderEntityList.get(fragmentsHolder.getCurrentFragmentIndex()).setSelected(true);
//        fragmentsHolder.switchFragment(2, getSupportFragmentManager(), R.id.main_fragment_container);
        adapter.notifyDataSetChanged();

        if (MainApplication.isApkInDebug(this) || MainApplication.isApkForTest()) {
            boolean debug = MainApplication.isApkInDebug(this);
            boolean test = MainApplication.isApkForTest();
            String msg = debug ? (test ? "Testing debug" : "poslink debug") : (test ? "Test dedicated" : "");
            FloatImageView floatImageView = findViewById(R.id.debug_float);
            floatImageView.setText(msg);
            floatImageView.setVisibility(View.VISIBLE);
            floatImageView.bringToFront();
        }

        GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_E);
    }

    private void askCameraPermission() {
        PermissionUtil.askPermission(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE}, new PermissionUtil.PermissionGrantCallback() {
            @Override
            public void onRefused() {
                DialogUtils.showWarnDialog(MainActivity.this, "Request Permission", "Application may not work properly", true);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FragmentsHolder.KEY_CURRENT_INDEX_OF_FRAGMENT, fragmentsHolder.getCurrentFragmentIndex());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    private void initTab() {
        ViewGroup tabTl = (ViewGroup) findViewById(R.id.main_tab_layout);
        ViewCompat.setElevation(tabTl, 9);
        final TabContainer tabContainer = new TabContainer(tabTl);
        adapter = new CommonBaseAdapter<>(renderEntityList);

        List<String> mTabName = Arrays.asList(getResources().getStringArray(R.array.tab_main));
        for (String tabName : mTabName) {
            if ("For Test".equals(tabName) && !Convenience.isButtonClickEnough()) {
                continue;
            }
            renderEntityList.add(new TabItemEntity(tabName, new TabItemEntity.OnItemClickCallback() {
                @Override
                public void onClick(View v, TabItemEntity renderEntity) {
                    fragmentsHolder.switchFragment(renderEntityList.indexOf(renderEntity), getSupportFragmentManager(), R.id.main_fragment_container);
                    renderEntity.setSelected(true);
                    for (int i = 0; i < renderEntityList.size(); i++) {
                        TabItemEntity tabItemEntity = renderEntityList.get(i);
                        if (tabItemEntity != renderEntity) {
                            tabItemEntity.setSelected(false);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }));
        }
        tabContainer.setAdapter(adapter);
    }

    private void initPopupWindow() {

        final View root = LayoutInflater.from(this).inflate(R.layout.layout_popup_window, null);
        root.measure(0,0);
        final int popHeight = root.getMeasuredHeight();
        final int popWidth = root.getMeasuredWidth();

        int w = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        popupWindow = new PopupWindow(root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());


        floatingActionButton = findViewById(R.id.floatBtn);

        floatingActionButton.measure(w, h);
        final int btnHeight = floatingActionButton.getMeasuredHeight();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(floatingActionButton, -popWidth, -(popHeight + btnHeight));
            }
        });

        Button startDetect = root.findViewById(R.id.btn_start_detect);
        Button startSwipe = root.findViewById(R.id.btn_start_swipe);
        Button stopDetect = root.findViewById(R.id.btn_stop);
        startDetect.setOnClickListener(this);
        startSwipe.setOnClickListener(this);
        stopDetect.setOnClickListener(this);

    }


    private void initExternalClass() {
        ExternalClass.delExtClass(ExternalClass.CLASSTYPE.UART_CASIO_VR100);
        ExternalClass.delExtClass(ExternalClass.CLASSTYPE.UART_CASIO_VR7000);

        Build bd = new Build();
        if (bd.MODEL.equals("V-R100")) {
            File caiosdevice_dex = new File(getApplicationContext().getFilesDir().getAbsolutePath()
                    + File.separator + "caiosdevice_dex.jar");
            if (!caiosdevice_dex.exists()) {
                //copy from assets
                caiosdevice_dex = copyExtJarFromAssets("caiosdevice_dex.jar", caiosdevice_dex.getAbsolutePath());
            }
            System.out.println("Loaded " + caiosdevice_dex.getAbsolutePath());
            DexClassLoader uart_cl = new DexClassLoader(caiosdevice_dex.getAbsolutePath(),
                    getApplicationContext().getFilesDir().getAbsolutePath(), null, getClassLoader());
            ExternalClass.addExtClass(new ExternalClass(uart_cl, ExternalClass.CLASSTYPE.UART_CASIO_VR100));
        } else if (bd.MODEL.equals("V-R7000")) {
            File caiosdevice_dex = new File(getApplicationContext().getFilesDir().getAbsolutePath()
                    + File.separator + "casioregdevicelibrary_dex.jar");
            if (!caiosdevice_dex.exists()) {
                //copy from assets
                caiosdevice_dex = copyExtJarFromAssets("casioregdevicelibrary_dex.jar", caiosdevice_dex.getAbsolutePath());
            }
            System.out.println("Loaded " + caiosdevice_dex.getAbsolutePath());
            DexClassLoader uart_cl = new DexClassLoader(caiosdevice_dex.getAbsolutePath(),
                    getApplicationContext().getFilesDir().getAbsolutePath(), null, getClassLoader());
            ExternalClass.addExtClass(new ExternalClass(uart_cl, ExternalClass.CLASSTYPE.UART_CASIO_VR7000));
        }
    }

    private File copyExtJarFromAssets(String name, String targetFile) {

        BufferedInputStream inBuff = null;
        FileOutputStream fs = null;
        InputStream inStream = null;
        try {
            inStream = getResources().getAssets().open(name);
            fs = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1444];
            int byteread = 0;
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            fs.flush();
        } catch (IOException e) {
            // Do nothing
        } finally {
            try {
                if (fs != null) fs.close();
            } catch (IOException e) {
                // Do nothing
            }
            try {
                if (inStream != null) inStream.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
        return new File(targetFile);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                KeyboardUtil.hideKeyboard(ev, view, this);
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private BroadPOSCommunicator.DetectAnyTimeListener detectAnyTimeListener = new BroadPOSCommunicator.DetectAnyTimeListener() {
        @Override
        public void onDetectCacheChange(int state) {
            switch (state) {
                case BroadPOSCommunicator.DetectAnyTimeListener
                        .STATE_NO_CACHE:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            floatingActionButton.setImageResource(R.mipmap.q);
                        }
                    });
                    break;
                case BroadPOSCommunicator.DetectAnyTimeListener
                        .STATE_HAS_INSERT_CACHE:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            floatingActionButton.setImageResource(R.mipmap.insert2);
                        }
                    });
                    break;
                case BroadPOSCommunicator.DetectAnyTimeListener
                        .STATE_HAS_SWIPE_CACHE:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            floatingActionButton.setImageResource(R.mipmap.swipe);
                        }
                    });
                    break;
                default:
                    //do nothing

            }
        }

        @Override
        public void onConnectService() {
            UIUtil.showToast(getApplicationContext(), "Start Success", Toast.LENGTH_SHORT);
        }

        @Override
        public void onFail(int code, String s) {
            UIUtil.showToast(getApplicationContext(), code + ", " + s, Toast.LENGTH_SHORT);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_detect:
                BroadPOSCommunicator.getInstance(this).startDetectAnyTime("11000000", detectAnyTimeListener);
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                break;
            case R.id.btn_start_swipe:
                BroadPOSCommunicator.getInstance(this).startDetectAnyTime("10000000", detectAnyTimeListener);
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                break;
            case R.id.btn_stop:
                BroadPOSCommunicator.getInstance(this).stopDetectAnyTime();
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                break;
            default:
                //do nothing
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                DialogUtils.showWarnDialog(MainActivity.this, "Request Permission", "Application may not work properly", true);
            }
        }
    }
}

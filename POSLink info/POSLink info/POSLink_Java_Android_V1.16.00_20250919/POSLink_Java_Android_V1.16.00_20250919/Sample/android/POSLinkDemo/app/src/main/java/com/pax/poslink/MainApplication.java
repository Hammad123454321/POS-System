package com.pax.poslink;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.widget.Toast;

import androidx.multidex.MultiDexApplication;

import com.pax.poslink.broadpos.BroadPOSReceiverHelper;
import com.pax.poslink.broadpos.ReceiverResult;
import com.pax.poslink.internal.Convenience;
import com.pax.poslink.log.CrashHandler;
import com.pax.poslink.log.LogStaticAndroidImpl;
import com.pax.poslink.util.LogStaticWrapper;
import com.pax.poslink.util.PermissionUtil;
import com.pax.poslink.util.UIUtil;
import com.pax.poslink.util.sharepref.SharedPrefKey;
import com.pax.poslink.util.sharepref.SharedPreferenceHelper;
import com.pax.poslink.util.thread.AppThreadPool;

/**
 * Created by Leon on 2017/4/28.
 */

public class MainApplication extends MultiDexApplication {

    private static MainApplication instance;

    public static MainApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            Context applicationContext = getApplicationContext();
//            IDAL dal = NeptuneDiamondUser.getInstance().getDal(applicationContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        init();
    }

    private void init() {
        PermissionUtil.init(getApplicationContext());
//        LogFilter.Const.DEBUG = BuildConfig.DEBUG;
        AppThreadPool.getInstance();
        SharedPreferenceHelper.instance(getApplicationContext());
        CommSetting commSetting = setupSetting(getApplicationContext());
        LogStaticWrapper.setLogImpl(new LogStaticAndroidImpl());
        POSLinkAndroid.init(getApplicationContext(), commSetting);
        android.util.Log.i("DEBUG", "Start Application");
        CrashHandler.init(getApplicationContext());
        Convenience.init(getApplicationContext());
        BroadPOSReceiverHelper.getInstance(this).setReceiverListener(new BroadPOSReceiverHelper.ReceiverListener() {
            @Override
            public void onReceiverFromBroadPOS(ReceiverResult receiverResult) {
                UIUtil.showToast(getApplicationContext(), receiverResult.getCode() + ": " + receiverResult.getMessage(), Toast.LENGTH_LONG);
                LogStaticWrapper.getLog().v("Message Form BroadPOS: " + receiverResult.getCode() + ", " + receiverResult.getMessage());
            }
        });
        LogStaticWrapper.getLog().v("DeviceMode:" + Build.MODEL);
    }

    private static CommSetting setupSetting(Context context) {
        String settingIniFile = context.getFilesDir().getAbsolutePath() + "/" + SettingINI.FILENAME;
        CommSetting commSetting = SettingINI.getCommSettingFromFile(settingIniFile);

        disableProxyForThisVersion(commSetting, settingIniFile);

        int index = UIUtil.findStringId(context.getResources().getStringArray(R.array.commSetting_types), commSetting.getType());
        if (index == -1) {
            if (Build.MODEL.startsWith("E")) {
                commSetting.setType(CommSetting.USB);
            } else if (Build.MODEL.startsWith("A")
                    || Build.MODEL.startsWith("IM") ){
                commSetting.setType(CommSetting.AIDL);
            } else {
                commSetting.setType(CommSetting.TCP);
            }
            commSetting.setTimeOut("60000");
            commSetting.setSerialPort("COM1");
            commSetting.setBaudRate("9600");
            commSetting.setDestIP("172.16.20.15");
            commSetting.setDestPort("10009");
            commSetting.setMacAddr("");
            commSetting.setEnableProxy(false);
            SettingINI.saveCommSettingToFile(settingIniFile, commSetting);
        }

        if (!SettingINI.loadSettingFromFile(settingIniFile)) {
            //String LogOutputFile = getApplicationContext().getFilesDir().getAbsolutePath() + "/POSLog.txt";
            String LogOutputFile = context.getExternalFilesDir(null).getPath();
            LogSetting.setLogMode(true);
            LogSetting.setLevel(LogSetting.LOGLEVEL.DEBUG);
            LogSetting.setOutputPath(LogOutputFile);
            SettingINI.saveLogSettingToFile(settingIniFile);
        }
        return SettingINI.getCommSettingFromFile(settingIniFile);
    }

    private static void disableProxyForThisVersion(CommSetting commSetting, String settingIniFile) {
        if (!SharedPreferenceHelper.get(SharedPrefKey.HAS_DISABLE_PROXY_ONCE, false)) {
            commSetting.setEnableProxy(false);
            SettingINI.saveCommSettingToFile(settingIniFile, commSetting);
            SharedPreferenceHelper.save(SharedPrefKey.HAS_DISABLE_PROXY_ONCE, true);
        }
    }

    /**
     * Determine if the current application is in debug state
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if it is a test version
     */
    public static boolean isApkForTest() {
        String versionName = BuildConfig.VERSION_NAME;
        CharSequence c = "T";
        return versionName.contains(c);
    }
}

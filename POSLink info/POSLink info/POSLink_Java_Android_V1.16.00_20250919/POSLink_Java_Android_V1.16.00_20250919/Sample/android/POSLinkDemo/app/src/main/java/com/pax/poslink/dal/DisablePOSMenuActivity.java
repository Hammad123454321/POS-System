package com.pax.poslink.dal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.Nullable;

import com.pax.poslink.R;
import com.pax.poslink.entity.POSMenu;
import com.pax.poslink.peripheries.MiscSettings;
import com.pax.poslink.ui.base.BaseActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Justin.Z on 2020-3-4
 */
public class DisablePOSMenuActivity extends BaseActivity {

    private Map<POSMenu, Boolean> posMenuBooleanMap = new HashMap<>();
    private Map<POSMenu, Integer> posMenuIntegerMap = new HashMap<>();

    {
        posMenuIntegerMap.put(POSMenu.ABOUT, R.id.checkbox_about);
        posMenuIntegerMap.put(POSMenu.ACCESSIBILITY, R.id.checkbox_accessibility);
        posMenuIntegerMap.put(POSMenu.ACCOUNTS, R.id.checkbox_accounts);
        posMenuIntegerMap.put(POSMenu.APPS, R.id.checkbox_apps);
        posMenuIntegerMap.put(POSMenu.BATTERY, R.id.checkbox_battery);
        posMenuIntegerMap.put(POSMenu.BT, R.id.checkbox_bt);
        posMenuIntegerMap.put(POSMenu.BT_TETHER, R.id.checkbox_bt_tether);
        posMenuIntegerMap.put(POSMenu.DATA_USAGE, R.id.checkbox_data_usage);
        posMenuIntegerMap.put(POSMenu.DATE, R.id.checkbox_date);
        posMenuIntegerMap.put(POSMenu.DISPLAY, R.id.checkbox_display);
        posMenuIntegerMap.put(POSMenu.GL_AIRPLANE, R.id.checkbox_gl_airplane);
        posMenuIntegerMap.put(POSMenu.GOOGLE, R.id.checkbox_google);
        posMenuIntegerMap.put(POSMenu.LANGUAGE, R.id.checkbox_language);
        posMenuIntegerMap.put(POSMenu.LOCATION, R.id.checkbox_location);
        posMenuIntegerMap.put(POSMenu.MEMORY, R.id.checkbox_memory);
        posMenuIntegerMap.put(POSMenu.MORE, R.id.checkbox_more);
        posMenuIntegerMap.put(POSMenu.NOTIFICATION, R.id.checkbox_notification);
        posMenuIntegerMap.put(POSMenu.PASSWORD, R.id.checkbox_password);
        posMenuIntegerMap.put(POSMenu.PRINTING, R.id.checkbox_printing);
        posMenuIntegerMap.put(POSMenu.PRIVACY, R.id.checkbox_privacy);
        posMenuIntegerMap.put(POSMenu.QS_AIRPLANE, R.id.checkbox_qs_airplane);
        posMenuIntegerMap.put(POSMenu.QS_BT, R.id.checkbox_qs_bt);
        posMenuIntegerMap.put(POSMenu.QS_DATA, R.id.checkbox_qs_data);
        posMenuIntegerMap.put(POSMenu.QS_DND, R.id.checkbox_qs_dnd);
        posMenuIntegerMap.put(POSMenu.QS_INVERSION, R.id.checkbox_qs_inversion);
        posMenuIntegerMap.put(POSMenu.QS_HOTSPOT, R.id.checkbox_qs_hotspot);
        posMenuIntegerMap.put(POSMenu.QS_LTE, R.id.checkbox_qs_lte);
        posMenuIntegerMap.put(POSMenu.QS_SAVER, R.id.checkbox_qs_saver);
        posMenuIntegerMap.put(POSMenu.QS_SETTING, R.id.checkbox_qs_setting);
        posMenuIntegerMap.put(POSMenu.QS_WIFI, R.id.checkbox_qs_wifi);
        posMenuIntegerMap.put(POSMenu.SECURITY, R.id.checkbox_security);
        posMenuIntegerMap.put(POSMenu.SIM, R.id.checkbox_sim);
        posMenuIntegerMap.put(POSMenu.STORAGE, R.id.checkbox_storage);
        posMenuIntegerMap.put(POSMenu.UPDATES, R.id.checkbox_updates);
        posMenuIntegerMap.put(POSMenu.WIFI, R.id.checkbox_wifi);


    }


    public static void start(Context context) {
        Intent starter = new Intent(context, DisablePOSMenuActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disable_posmenu);
        initView();
    }

    private void initView() {

        findViewById(R.id.btn_disable_pos_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPOSMenuMap(true);
                MiscSettings.disablePosMenu(getApplicationContext(), posMenuBooleanMap);
            }
        });

        findViewById(R.id.btn_enable_pos_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPOSMenuMap(false);
                MiscSettings.disablePosMenu(getApplicationContext(), posMenuBooleanMap);
            }
        });
    }

    private void setPOSMenuMap(boolean flag) {
        POSMenu[] posMenus = POSMenu.values();
        for (POSMenu posMenu : posMenus) {
            CheckBox checkBox = findViewById(posMenuIntegerMap.get(posMenu));
            if (checkBox.isChecked()) {
                posMenuBooleanMap.put(posMenu, flag);
            }
        }
    }
}

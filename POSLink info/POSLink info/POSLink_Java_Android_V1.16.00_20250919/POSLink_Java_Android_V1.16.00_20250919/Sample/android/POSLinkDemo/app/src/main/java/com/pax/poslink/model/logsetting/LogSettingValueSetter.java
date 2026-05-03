package com.pax.poslink.model.logsetting;

import com.pax.poslink.LogSetting;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leon.F on 2018/6/8.
 */
public interface LogSettingValueSetter {


    String LOG_SWITCH = "Log Switch";
    String LOG_LEVEL = "Log Level";
    String LOG_FILE_PATH = "Log File Path";
    String LOG_FILE_NAME = "Log File Name";
    String LOG_DAYS = "Log days";
    Map<String, LogSettingValueSetter> VALUE_SETTER_MAP = new HashMap<String, LogSettingValueSetter>() {
        {

            put(LOG_SWITCH, new LogSettingValueSetter() {
                @Override
                public void onSet(String value) {
                    LogSetting.setLogMode("ON".equalsIgnoreCase(value));
                }
            });
            put(LOG_LEVEL, new LogSettingValueSetter() {
                @Override
                public void onSet(String value) {
                    LogSetting.setLevel(LogSetting.LOGLEVEL.valueOf(value));
                }
            });
            put(LOG_FILE_PATH, new LogSettingValueSetter() {
                @Override
                public void onSet(String value) {
                    LogSetting.setOutputPath(value);
                }
            });
            put(LOG_FILE_NAME, new LogSettingValueSetter() {
                @Override
                public void onSet(String value) {
                    LogSetting.setLogFileName(value);
                }
            });
            put(LOG_DAYS, new LogSettingValueSetter() {
                @Override
                public void onSet(String value) {
                    LogSetting.setLogDays(value);
                }
            });
        }
    };

    void onSet(String value);
}

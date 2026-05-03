package com.poslink.sample.setting.model;

import com.pax.poslink.LogSetting;
import com.poslink.sample.common.FileUtil;
import com.poslink.sample.common.JsonUtil;

import java.io.File;
import java.io.IOException;
/**
 * Created by Leon.F on 2018/3/13.
 */

public class LogSettingDao {
    private static final String SETTING_PATH = "./log_setting.json";

    public static void save(LogSettingRequest setting) throws IOException {
        String json = JsonUtil.toJson(setting);
        FileUtil.createFile(SETTING_PATH, json);
        setPOSLinkSetting(setting);
    }

    private static void setPOSLinkSetting(LogSettingRequest setting) {
        LogSetting.setLogMode(!BaseLogSetting.DISABLE.equalsIgnoreCase(setting.getLogSwitch()));
        LogSetting.setLogFileName(setting.getLogFileName());
        LogSetting.setOutputPath(setting.getLogFilePath());
        LogSetting.setLevel(LogSetting.LOGLEVEL.valueOf(setting.getLogLevel()));
        LogSetting.setLogDays(setting.getLogDays());
    }

    public static LogSettingRequest read() {
        if (!new File(SETTING_PATH).exists()) {
            LogSettingRequest logSettingRequest = new LogSettingRequest();
            logSettingRequest.setLogSwitch(LogSetting.isLoggable() ? BaseLogSetting.ENABLE : BaseLogSetting.DISABLE);
            logSettingRequest.setLogFileName(LogSetting.getLogFileName());
            logSettingRequest.setLogFilePath(LogSetting.getOutputPath());
            logSettingRequest.setLogLevel(LogSetting.getLevel().name());
            logSettingRequest.setLogDays(LogSetting.getLogDays());
            return logSettingRequest;
        }
        String json = FileUtil.readByLines(SETTING_PATH);

        LogSettingRequest settingRequest = null;
        settingRequest = JsonUtil.fromJson(json, LogSettingRequest.class);
        // Save if exist
        setPOSLinkSetting(settingRequest);
        return settingRequest;
    }
}
